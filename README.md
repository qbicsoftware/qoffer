# Qoffer-Portlet

[![Build Status](https://travis-ci.com/qbicsoftware/qoffer-portlet.svg?branch=development)](https://travis-ci.com/qbicsoftware/qoffer-portlet)[![Code Coverage]( https://codecov.io/gh/qbicsoftware/qoffer-portlet/branch/development/graph/badge.svg)](https://codecov.io/gh/qbicsoftware/qoffer-portlet)

Qoffer-Portlet, version 1.1-SNAPSHOT - The QBiC Offer Manager allows the user to create offers via the Offer Generator Tab, manage and print existing offers via the Offer Manager Tab
and create and manage packages for the offers via the Package Manager Tab.

## Author
Created by Benjamin Sailer, Jennifer Boedker (jennifer.boedker@student.uni-tuebingen.de).

## Description
### Offer Generator

The Offer Generator Tab allows the user to generate offers for the
available openBIS projects and also to add the packages at the same time.
The packages can then be edited via Offer Generator tab (change quantity,
add/remove packages) or via the Package Manager tab (change package name,
description, price, etc.).

#### Generating an offer:

1. **Select a project** from the list of available projects. Note: You can
filter the projects by typing the openBIS id of the project you want
to create the offer for in the ComboBox.
2. **Add the desired packages to the project** by either double clicking
or using the arrow buttons. The packages can also be filtered by their
name and/or their id using the TextField.
3. **Generate the offer** by hitting the complete button.


### Offer Manager

The Offer Manager Tab allows the user to view all the offers stored in the
database, edit some fields (e.g. offer name, description, etc.) and
adding/removing the packages for the offers. Furthermore the user can
update the package quantity for the currently selected offer and also
change the price (internal/external academic/external commercial) the
package should use. Additionally the user can
generate a .docx file for the current offer and download it.

#### Editing an offer:

The following operations are supported via the edit menu (opens
by either selecting an offer and hitting shift + enter or by double
clicking an offer):
* **Editing the offer name**
* **Editing the offer description**
* **Editing the offer prospect**

The following operations are supported via the control bar below the
offer grid:
* **Editing the offer status:** Select an offer on the grid and the status
to set via the *Select Status* ComboBox, then hit the *Update* button
* **Deleting an offer:** Select an offer on the grid, make sure you really
want to delete that offer and hit the *Delete* button

The following operations are supported via the control bar at the
bottom. **Note #1:** This control bar is only shown when an offer has been
selected! **Note #2:** You can change the packages to display via the Select
package group ComboBox. E.g. if you want to see only the *Sequencing*
packages below, simply select *Sequencing* in the *Select Package group*
ComboBox.
* **Updating the package quantity for the current offer:** Select the
package you want to update the quantity for and hit the *Update
quantity* button. **Note**: For sequencing packages there is an
automatic discount based on the number of samples applied (see the
README in the src folder for a detailed explanation).
* **Adding a package to the offer:** Select the package you want to add
to the offer via the ComboBox, then hit the *Add* button right next to it.
* **Removing a package from the offer:** Select the package you want to
remove in the grid, then hit remove.
* **Updating which price to use (internal, external academic, external
commercial):** Select the package you want to update the price type for
and the new price type via the the *External/Internal Price* ComboBox and
hit the *Update price type* button.

#### Generating the .docx file:

Generating the .docx file is rather easy:
 1. **Select an offer**.
 2. **Hit the *Generate Offer* button**.
 3. **Download the generated file** via the dialog.

The implementation details can be found [here](https://github.com/qbicsoftware/qoffer/blob/master/src/README.md).


### Package Manager

The package manager allows the user to add new packages and also to edit
or delete existing ones.

#### Package management:

The following operations are supported for the packages:

* **Adding a package:** Simply hit the *New Package* button and edit the
new package as desired.
* **Delete a package:** Select the package you want to remove and hit
the *Delete* button. Note: Deleting a package which is used by an offer
is **NOT** possible. If you need to delete it, remove it from the
associated offers first (via the Offer Manager tab), then hit the
delete button.
* **Editing a package:** See below for a detailed list of supported
operations.

#### Editing a package:

Editing a package is usually done via the inline editor. To open the
editor either select the package and hit shift \+ enter (works more
consistently) or double click the package.

The following operations are supported for editing the packages:

* **Editing the package name**: Via inline editor (see above).
* **Editing the package description**: Via inline editor (see above).
* **Editing the package facility**: Via inline editor (see above).
* **Editing the package price**: Via inline editor (see above). Also see
section *Automatic price calculation* below.
* **Editing the package unit type**: Via inline editor (see above).
* **Editing the package group**: Select a package to edit on the grid,
 the new package group via the *Select package group* ComboBox and
hit the *Update* button.


#### Automatic price calculation:

qOffer supports automatic calculation of the external academic and
external commercial price based on the internal price. To enable this
check the *Auto-calculate external prices* check box and edit the
internal price via the inline editor (see above). **Note:** It might be
necessary to open the inline editor via selecting the row and hitting
shift \+ enter rather than double clicking, since Vaadin sometimes fails
to grab the currently selected row if selected via double click. The
detailed README on the implementation details can be found [here](https://github.com/qbicsoftware/qoffer/blob/master/src/README.md).


## How to Install
