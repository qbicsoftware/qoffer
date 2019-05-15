package life.qbic.dbase;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import java.util.List;
import life.qbic.portal.utils.ConfigurationManager;
import life.qbic.portal.utils.ConfigurationManagerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Singleton that encapsulates access to openBIS.
 */
public class OpenBisProxy {

  private static final Logger LOG = LogManager.getLogger(OpenBisProxy.class);

  private static OpenBisProxy INSTANCE;
  private static final int TIMEOUT = 100000;

  private String sessionToken;
  private final IApplicationServerApi apiStub;
  private final String user;
  private final String password;

  private OpenBisProxy(final String url, final String user, final String password) {
    this.user = user;
    this.password = password;
    this.apiStub = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, url, TIMEOUT);
    LOG.info("OpenBisProxy instance created");
  }

  // logs into openBIS if needed
  private void activateSession() {
    if (StringUtils.isBlank(sessionToken) || !apiStub.isSessionActive(sessionToken)) {
      LOG.info("Logging into openBIS");
      sessionToken = apiStub.login(user, password);
    }
  }

  public List<Project> getProjects() {
    LOG.info("Querying for openBIS projects");
    final SearchResult<Project> searchResult = apiStub.searchProjects(sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions());
    LOG.info("Found {} openBIS projects", searchResult.getTotalCount());
    return searchResult.getObjects();
  }

  public String getProjectDescription(final String projectCode) {
    final ProjectSearchCriteria projectSearchCriteria = new ProjectSearchCriteria();
    projectSearchCriteria.withCode().thatEquals(projectCode);
    final SearchResult<Project> searchResult = apiStub.searchProjects(sessionToken, projectSearchCriteria, new ProjectFetchOptions());
    if (searchResult.getTotalCount() == 0) {
      throw new RuntimeException("Could not find project with code " + projectCode);
    }
    if (searchResult.getTotalCount() > 1) {
      LOG.warn("Obtained {} projects for code {}. Was expecting only one! Will return the first one.", searchResult.getTotalCount(), projectCode);
    }
    String description = "if you are reading this, report it ASAP because you just found an error in the java compiler";
    for (final Project p : searchResult.getObjects()) {
      description = p.getDescription();
      break;
    }
    return description;
  }

  public static synchronized OpenBisProxy getInstance() {
    init();
    INSTANCE.activateSession();
    return INSTANCE;
  }

  private static void init() {
    LOG.info("Initializing OpenBisProxy");
    if (INSTANCE == null) {
      final String password, username, url;
      //TODO local properties file path is now: src/main/resources/developer.properties

//      if (isLiferayPortlet()) {
        final ConfigurationManager conf = ConfigurationManagerFactory.getInstance();
        password = conf.getDataSourcePassword();
        username = conf.getDataSourceUser();
        url = conf.getDataSourceApiUrl() + IApplicationServerApi.SERVICE_URL;
        //LOG.info("OpenBIS URL {}",url);
//      } else {
//        try (final InputStream input = new FileInputStream(qOfferManagerUtils.PROPERTIES_FILE_PATH)) {
//
//          // load a properties file
//          final Properties prop = new Properties();
//          prop.load(input);
//
//          password = prop.getProperty(LiferayConfigurationManager.DATASOURCE_PASS);
//          username = prop.getProperty(LiferayConfigurationManager.DATASOURCE_USER);
//          url = prop.getProperty(LiferayConfigurationManager.DATASOURCE_API_URL) + IApplicationServerApi.SERVICE_URL;
//
//        } catch (IOException ex) {
//          throw new RuntimeException("Could not read configuration settings", ex);
//        }
//      }
      INSTANCE = new OpenBisProxy(url, username, password);
    }
  }

}
