# Replace Templates

In order to properly include the new Template.docx into the portlet one needs bind the textfields to an XML whereby the XML serves as content control.
Then it is possible to use *Docx4j* to let your code change the text within these fields.

## Prerequisite

For this task we need *Microsoft Word* and *OpenDoPE* (you can find the add-in here: https://www.opendope.org/implementations.html).

## Step by Step

After downloading OpenDoPE the add-in is automatically added as a tab to Word. 
Click that tab and create a XML for your Template.docx by clicking **"Add CustomXML to this docx"**. Answer **"no"** to the following dialog. 
The OpenDoPE taskpane opens on the right side.

![setupDialog](https://github.com/qbicsoftware/qoffer-portlet/blob/development/src/main/webapp/WEB-INF/images/setupDialog.png)


#### 1) Select

Select the text that you want to bind. Then click **"Bind this text"** on the upper task pane.

![BindThisText](https://github.com/qbicsoftware/qoffer-portlet/blob/development/src/main/webapp/WEB-INF/images/bindThisText.png)

#### 2) Bind

The OpenDoPE task pane then shows some details:

![bindThis...](https://github.com/qbicsoftware/qoffer-portlet/blob/development/src/main/webapp/WEB-INF/images/bindThis.png)

Click **"Bind this ..."** (this takes some time the first time this is executed for a document)

#### 3) XML

The XML appears within the task pane. A default XML is already created, the root is *yourXML* you can now edit this XML as you go on by adding new bindings. Add an XML node for the text you want to bind by typing:
```xml 
<myNewNode> Default Value </myNewNode> 
```
The default value will be placed into the textfield, if it changes in the XML it also cahnges in the document. Please use proper naming here since the node name is the name that you later use within the program code.

![XML](https://github.com/qbicsoftware/qoffer-portlet/blob/development/src/main/webapp/WEB-INF/images/XML.png)

To determine the XPath you can click the node in the XML and Word automatically adds this path (or you can manually type it).
You need to give an ID to the path. Since I don't know what effect the naming of the ID has I used to name it like the node. Finally click **"Use this XPath"**, your text is now bound to the XPath.
Keep in mind that after that you cannot use the same XPathID for *another* path again (but indeed use the same XPath and XPathID for another textfield which should display the same content). Furthermore there seems to be no way to completely delete already bound XPathIDs, thus if you mess up with the naming it is probably better to start from scratch. 

You can also add parent nodes like this:
```xml 
<myParentNode>
	<myNewNode> Default Value </myNewNode> 
</myParentNode>
```
but consider that after a node has already been bound to an XPath the changing of a the XML hierachy is not valid if it affects this node.

#### Table Content (Repeats)

If you want to bind a table rows you need to make use of content repeats.
The naming of the nodes for the offer table are predefined in the code (see *XMLUtils.java*). The table is represented in the XML as **work_package_table** a new row is indicated with **row**:

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

Now we need to tell Word that the node **row** can multiply occur in our XML. 
First, contents of each column of the example row are separately bound as text (see *Steps 1)-3)*). Then, the complete row is selected. Click "**Wrap with Repeat"**. 

![wrapWithRepeat](https://github.com/qbicsoftware/qoffer-portlet/blob/development/src/main/webapp/WEB-INF/images/wrapWithRepeat.png)

Select **"Make Repeat"** in the task pane.

![makeRepeat](https://github.com/qbicsoftware/qoffer-portlet/blob/development/src/main/webapp/WEB-INF/images/makeRepeat.png)

Again the XML appears in the task pane. You choose the XPath of **row** and name the ID e.g. 'repeatRow'. 
By clicking **"show Tags"** you can display the bindings of the document. The binding of the table should look like this:

![table](https://github.com/qbicsoftware/qoffer-portlet/blob/development/src/main/webapp/WEB-INF/images/tableSmall.png)

SAVE the document!


## Find your CustomXML

Unzipp the docx file. Within this folder you will find the folder **customXml** that contains the final XML which is your content control. 
The folder shows multiple itemX.xml files. One of them is your desired XML (the XML is randomly saved to one of those itemX files .. happy searching). You now need to include the XML with the bound docx into the source code.

## Include the new Template into the Code

Check out the class *OfferMangagerTab.java*, here you should replace the templates (line ~457). The XML nodes are used within the code. Thus, if you are using different node names as in the old template you also need to adjust the node values (line ~550).

If the format of the offer table is changed e.g. additional columns are added you also need to check *XMLUtils.java*. This class defines how the new rows are created and added to the XML (it makes also use of the XML nodes you have defined).

