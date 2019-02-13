# Implementation details:

## Installation
This project requires both openBIS and a separate database. Make sure your configuration files (properties files) are up to date. 

The SQL script under `src/main/resources/sql/tables.sql` contains the required tables. Run this script before deploying the portlet for the first time.

### Package discount based on package quantity:

For the sequencing packages (packages with package group *Sequencing*)
there is an discount based on the number of samples used.
The discount per sample size can be found in the file [discount_per_sample_size.csv](https://github.com/qbicsoftware/qoffer/blob/master/src/main/webapp/WEB-INF/resourceFiles/discount_per_sample_size.csv). The
discount is taken from this file and then automatically applied to the
total package price and thus also the total offer price.

### Generate the .docx file:

#### Background:

Generating the .docx file is implemented with *content control data
binding*. Content control is a container for content and a standard feature
of the .docx fileformat. Using content control data binding one can bind
data stored in a .xml file to such containers, so the bound data is
displayed in the .docx file. Essentially we have several placeholders,
the containers, and all the values we want for the placeholder.

Unfortunately there is no way to change the style of the
placeholder based on the values itself. To change the style you need to
change the style of the placeholder. Also it's not possible to bind
more complicated structures (e.g. hyperlinks).

For qOffer we have two important files:
* Template.docx: holds all the placeholders to be replaced
* contentControlTemplate.xml: holds all the values the placeholders
should be replaced with

Both files were generated with the Word plugin OpenDoPE for Microsoft
Word 2007 (see www.opendope.org/ and
http://www.opendope.org/WordAddIn_walkthrough.pdf).

#### Implementation:

When a user wants to create the .docx file and clicks on the
*Generate Offer* button, the following steps are performed:
* Grabbing the informating from the grid (Client name, address, etc.;
offer name, description, etc.; package names, prices, etc.)
* Reading the contentControlTemplate.xml file and editing the respective
fields within it using the XMLUtils class
* Applying the bindings of the contentControlTemplate.xml to the
Template.docx file using the Docx4jUtils class
* Saving the resulting .docx file and enabling the download via the
FileResourceHandler

**Note:** If you run the portlet on your local machine, you probably
need to change the variable *pathOnServer* at the top (~l. 50) of the
*OfferManagerTab.java* according to your file system.

### Automatic price calculation:

The following table shows the price modifiers used for the different
customers (internal, external academic, external commercial) and the
different package groups. **Note:** For the package group "Other" there
is no automatic price calculation available.

Package group | Internal | External Academic | External Commercial
--- | --- | --- | ---
Bioinformatics Analysis/Project Management | 1.0 | 1.3 | 2.0
Sequencing/Mass Spectrometry | 1.0 | 1.5 | 1.5
Other | 1.0 | - | -

The prices are automatically calculated when the check box
*Auto-calculate external Prices* is enabled and the user uses
the inline editor to edit a package. **Note:** The automatic price
calculation only refers to the package the user is editing, NOT
any other packages.

The price modifiers can be easily adjusted in the PackageManagerTab.java
file via three variables at the top.

## Database:

On portal-testing we use the *facs_facility* database. The important
information is stored in three tables: *offers*, *packages* and
*offers_packages*.

To run the database locally on your system, create a empty text file
"Credentials.properties" in the webapp/WEB-INF/resourceFiles/ directory and replace the
username and password placeholders below with the actual username and
password: <br/>

mysql.username=actual_username <br/>
mysql.password=actual_password


### Offers:

Holds the current offers:

Field | Description | Example
--- | --- | ---
offer_id | Unique Id of the offer.	 | 15
offer_number | QBiC id of the offer: <date\>_<projectReference\>	 | 20170815_QHUCW
offer_project_reference | Id of the project.	 | QHUCW
offer_facility | Name of the prospect.	 | Mr. John Doe
offer_name | Name of the offer.	 | Gene Expression Analysis of CD96 using RNA-Seq
offer_description | Description of the offer.	 | Objective: The CD96 receptor is presumed to be involved in the HIV molecular cascade in Human. [...]
offer_group | Not in use.	 | -
offer_price | Offer price in € without discounts/taxes. 	 | 11762
offer_total | Total offer price in € including taxes/discounts (**NO** taxes/flat-rate discounts for the complete offer are applied at the current state (05/2018) of the qOffer).	 | 11762
offer_date | Creation date of the offer	 | **Old format:** 2018-04-24;<br/> **New format:** 2018-04-24 11:24:05.0
offer_status | Status of the offer 	 | Enum:<br/> ["In Progress", "Sent", "Accepted", "Rejected"]
last_edited | Date of the latest edit	 | 2018-04-24 13:48:00.0
added_by | Liferay user id  	 | user42
discount | **Deprecated!** Discount of the offer in percentage.	 | 10%
internal | **Deprecated!** Boolean whether offer is internal/external.	 | 1
offer_extra_price | Not in use.	 | -

### Packages:

Holds all of the available packages:

Field | Description | Example
--- | --- | ---
package_id | Unique Id of the package.	 | 8
package_name | Name of the package. 	 | Quality control of raw reads/data
package_facility | Facility which realizes the package. 	 | e.g. QBiC, IMGAG, CeGaT
package_description | Description of the package.	 | Quality control of raw reads/data (filtering,trimming,adapter removal).
package_group | Group the package belongs to. 	 | ["Sequencing", "Project Management", "Bioinformatics Analysis", "Mass spectrometry", "Other"]
package_grp | **Deprecated!** Group the package belongs to.	 | Sequencing
package_price_internal | Package price in € for internal customers.	 | 42
package_price_external_academic | Package price in € for external academic customers.	 | 54.6
package_price_external_commercial | Package price in € for external commercial customers.	 | 84
package_unit_type | How the price is being applied. 	 | E.g. "per sample" or "per GB".
package_date | Date of the package creation, not really in use.	 | 2017-10-26
last_edited | Date of the latest edit. 	 | 2018-05-04 14:14:03.0
added_by | Not in use.	 | -

### Offers_Packages:

Connects the offers and the packages table and holds one entry for each
package for each offer. Also holds the total package price for that specific offer
and for the sequencing packages the sample discount.

Field | Description | Example
--- | --- | ---
offer_id | Id of the offer.	 | 21
package_id | Id of the package.	 | 32
package_addon_price | Total package price:<br/> ((base price * count) * (1-discount)).	 | 1595.24
package_count | Package count; <br/> for sequencing packages = #samples. 	 | 95
package_discount | Discount of the package in percent.	 | 69%
package_price_type | What package price to use (internal, external academic, external_commercial).	 | Enum: <br/> ["internal", "external_academic", "external_commercial"]


## Known issues/bugs:

    - OfferGenerator.java: ValueChange event does not trigger if the last
    package is being removed from the right side of the TwinColSelect.
    -> creating the offer will include this package, although it has been
    deselected by the user
    - PackageManager.java: Getting all the offer_ids where the package
    is in use takes 5-10s of time. Thus it has been disabled.
    (See: "// TODO: offer_ids currently not in use; since it's extremely slow..")
    - Opening the inline-editor via double click is sometimes a bit tricky,
    since vaadin often fails to grab the selected row, but I guess there's
    not much one do about it (except using shift + enter to open it)..

## TODO:

    - Delete generated .docx files after some time or create temporary files
    - Store generated .docx files as BLOB in database
    - Adding a lot of packages breaks the generated .docx file

