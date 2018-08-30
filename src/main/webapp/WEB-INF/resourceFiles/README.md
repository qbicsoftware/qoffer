# Including a new Template

In order to properly include the new Template.docx into the portlet one needs bind the textfields to an XML.
Then it is possible to use docx4j to let your portlet change the text within these fields.

## Prerequisite

For this task we are going to need Microsoft Word and OpenDoPE (you can find the add-in here: https://www.opendope.org/implementations.html).

## Step by Step

After downloading OpenDoPE the Add-in is automatically added as tab to Word. 
Click that tab and create a XML for the Template.docx by clicking "Add CustomXML to this docx". Answer "no" to the following dialog. 
The OpenDoPE taskpane opens on the right side.

![setupDialog](https://github.com/qbicsoftware/qoffer-portlet/blob/feature/fixBug/src/main/webapp/WEB-INF/images/setupDialog.png)


### Select

Select the text that you want to bind. Then click "Bind this text" on the upper task pane.

![BindThisText](https://github.com/qbicsoftware/qoffer-portlet/blob/feature/fixBug/src/main/webapp/WEB-INF/images/bindThisText.png)

### Bind

The OpenDoPE task pane then shows some details:

![bindThis...](https://github.com/qbicsoftware/qoffer-portlet/blob/feature/fixBug/src/main/webapp/WEB-INF/images/bindThis.png)

Click "Bind this ..." (this takes some time the first time this is executed for a document)

### XML

The XML appears inside the task pane. You can now directly add an XML node for the text you want to bind. Please use proper naming here since the node name is the name that you later use within the program code.

![XML](https://github.com/qbicsoftware/qoffer-portlet/blob/feature/fixBug/src/main/webapp/WEB-INF/images/XML.png)

To determine the XPath you can click the node in the XML and Word automatically adds this path (or you can manually type it).
You need to give the an ID to the path. Since I don't know what effect the naming of the ID has I used to name it like the node. Finally click "Use this XPath", your text is now bound to the XPath.
Keep in mind that after that you cannot use the same XPath for another node again. Furthermore the seems to be no way to completely delete already bound XPaths, thus if you mess up with the naming sometimes it is better to start from scratch. 


### Table Content (Repeats)

If you want to bind a table you need to make use of content repeats.
The nodes for the offer table are predefined in the code (see XMLUtils.java). The table is represented in the XML as **'work_package_table'** a new row is indicated with **'row'**:

```xml
<myXML>
	<work_packages_table>
		<row>
			<work_package_name>Sequencing by Sequencing Provider... (internal offer no.: 2017-7816)</work_package_name>
			<work_package_quantity>1</work_package_quantity>
			<work_package_unit_price>4165.00 €</work_package_unit_price>
			<work_package_amount>4165.00 €</work_package_amount>
		</row>
</work_packages_table>
	</myXML>
``` 

Now we need to tell Word that row can multiply appear in our XML. 
First, contents of each column of the example row are separately bound as text. Then, the complete row is selected. Click "Wrap with Repeat". 
(one example row is enough since these rows are deleted anyway)

![wrapWithRepeat](https://github.com/qbicsoftware/qoffer-portlet/blob/feature/fixBug/src/main/webapp/WEB-INF/images/wrapWithRepeat.png)

Like before the task pane shows further details:

![makeRepeat](https://github.com/qbicsoftware/qoffer-portlet/blob/feature/fixBug/src/main/webapp/WEB-INF/images/makeRepeat.png)

choose "Make Repeat". Again the XML appears in the task pane. Now you choose the path of 'row' as XPath and name the ID e.g. 'repeatRow'. 
By clicking "show Tags" you can display the bindings of the document. The binding of the table should look like this:

![table](https://github.com/qbicsoftware/qoffer-portlet/blob/feature/fixBug/src/main/webapp/WEB-INF/images/table.png)


## Include the new Template into the Code

The XML nodes are used within the code thus if you are using different node names as in the old templates you need to adjust them in the code. Check out the class "OfferMangagerTab.java".
Here you replace the templates (line ~457) and also change the XML nodes (line ~550).

If the offer table e.g. additional columns you also need to check "XMLUtils.java". This class defines how the new rows are created and added to the XML.