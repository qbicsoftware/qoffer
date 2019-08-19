package life.qbic.utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Helps compute price modifications based on customer group and type of work package
 * 
 * @author afriedrich
 *
 */
public class PriceModificationHelper {

  private static final Logger LOG = LogManager.getLogger(PriceModificationHelper.class);

  // TODO should probably read these in via a config file at some point
  static final Map<String, BigDecimal> modifiers = new HashMap<String, BigDecimal>() {
    {
      put("external_academicsSequencing", new BigDecimal(1.2));
      put("external_academicsMass Spectrometry", new BigDecimal(1.2));
      put("external_academicsBioinformatics Analysis", new BigDecimal(1.2));
      put("external_academicsProject Management", new BigDecimal(1.2));

      put("external_commercialSequencing", new BigDecimal(1.4));
      put("external_commercialMass Spectrometry", new BigDecimal(1.4));
      put("external_commercialBioinformatics Analysis", new BigDecimal(1.4));
      put("external_commercialProject Management", new BigDecimal(1.4));
    }
  };

  /**
   * Helps compute price modifications based on internal price, customer group and type of work
   * package. Returns the internal price if the input combination of customer and work package is
   * unknown, meaning no price modification.
   * 
   * @param internal the internal price
   * @param customerGroup
   * @param packageGroup
   * @return
   */
  public static BigDecimal computePrice(BigDecimal internal, String customerGroup,
      String packageGroup) {
    if (customerGroup == null || packageGroup == null) {
      return internal;
    }
    String key = customerGroup + packageGroup;
    BigDecimal val = modifiers.get(key);
    if (val != null) {
      return val.multiply(internal);
    } else {
      LOG.info("could not modify price because key of package group (" + packageGroup
          + ") and customer group (" + customerGroup + ") was unknown. Returning original price.");
      return internal;
    }
  }


}

