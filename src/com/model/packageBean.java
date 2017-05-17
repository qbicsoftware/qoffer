/*******************************************************************************
 * QBiC Offer Generator provides an infrastructure for creating offers using QBiC portal and
 * infrastructure. Copyright (C) 2017 Aydın Can Polatkan
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see http://www.gnu.org/licenses/.
 *******************************************************************************/

package com.model;

import java.io.Serializable;
import java.sql.Timestamp;


public class packageBean implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -5000536883849358850L;
  private int package_id;
  private String package_name;
  private String package_facility;
  private String package_description;
  private String package_group;
  private double package_price;
  private double package_price_external;
  private String package_unit_type;
  private Timestamp package_date;

  public packageBean(int package_id, String package_name, String package_facility,
      String package_description, String package_group, double package_price,
      double package_price_external, String package_unit_type, Timestamp package_date) {
    super();
    this.package_id = package_id;
    this.package_name = package_name;
    this.package_facility = package_facility;
    this.package_description = package_description;
    this.package_group = package_group;
    this.package_price = package_price;
    this.setPackage_price_external(package_price_external);
    this.setPackage_unit_type(package_unit_type);
    this.package_date = package_date;
  }

  public packageBean() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @return the package_id
   */
  public int getpackage_id() {
    return package_id;
  }

  /**
   * @param package_id the package_id to set
   */
  public void setpackage_id(int package_id) {
    this.package_id = package_id;
  }

  /**
   * @return the package_name
   */
  public String getpackage_name() {
    return package_name;
  }

  /**
   * @param package_name the package_name to set
   */
  public void setpackage_name(String package_name) {
    this.package_name = package_name;
  }

  /**
   * @return the package_facility
   */
  public String getpackage_facility() {
    return package_facility;
  }

  /**
   * @param package_facility the package_facility to set
   */
  public void setpackage_facility(String package_facility) {
    this.package_facility = package_facility;
  }

  /**
   * @return the package_description
   */
  public String getpackage_description() {
    return package_description;
  }

  /**
   * @param package_description the package_description to set
   */
  public void setpackage_description(String package_description) {
    this.package_description = package_description;
  }

  /**
   * @return the package_group
   */
  public String getpackage_group() {
    return package_group;
  }

  /**
   * @param package_group the package_group to set
   */
  public void setpackage_group(String package_group) {
    this.package_group = package_group;
  }

  /**
   * @return the package_price
   */
  public double getpackage_price() {
    return package_price;
  }

  /**
   * @param package_price the package_price to set
   */
  public void setpackage_price(double package_price) {
    this.package_price = package_price;
  }

  /**
   * @return the package_price_external
   */
  public double getPackage_price_external() {
    return package_price_external;
  }

  /**
   * @param package_price_external the package_price_external to set
   */
  public void setPackage_price_external(double package_price_external) {
    this.package_price_external = package_price_external;
  }

  /**
   * @return the package_unit_type
   */
  public String getPackage_unit_type() {
    return package_unit_type;
  }

  /**
   * @param package_unit_type the package_unit_type to set
   */
  public void setPackage_unit_type(String package_unit_type) {
    this.package_unit_type = package_unit_type;
  }

  /**
   * @return the package_date
   */
  public Timestamp getpackage_date() {
    return package_date;
  }

  /**
   * @param package_date the package_date to set
   */
  public void setpackage_date(Timestamp package_date) {
    this.package_date = package_date;
  }



}
