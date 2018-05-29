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

package life.qbic.model;

import java.io.Serializable;
import java.sql.Timestamp;


/**
 * TODO: read below
 * Bean for an offer. NOTE: currently NOT in use; but in the long run using a class for the offers would be probably
 * better than multiple arrayLists.
 */
public class offerBean implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -5000536883849358850L;
  private int offer_id;
  private String offer_name;
  private String offer_facility;
  private String offer_description;
  private String offer_group;
  private double offer_price;
  private Timestamp offer_date;
  private boolean offer_status;

  public offerBean(int offer_id, String offer_name, String offer_facility,
      String offer_description, String offer_group, double offer_price, Timestamp offer_date,
      boolean offer_status) {
    super();
    this.offer_id = offer_id;
    this.offer_name = offer_name;
    this.offer_facility = offer_facility;
    this.offer_description = offer_description;
    this.offer_group = offer_group;
    this.offer_price = offer_price;
    this.offer_date = offer_date;
    this.setOffer_status(offer_status);
  }

  /**
   * @return the offer_id
   */
  public int getOffer_id() {
    return offer_id;
  }

  /**
   * @param offer_id the offer_id to set
   */
  public void setOffer_id(int offer_id) {
    this.offer_id = offer_id;
  }

  /**
   * @return the offer_name
   */
  public String getOffer_name() {
    return offer_name;
  }

  /**
   * @param offer_name the offer_name to set
   */
  public void setOffer_name(String offer_name) {
    this.offer_name = offer_name;
  }

  /**
   * @return the offer_facility
   */
  public String getOffer_facility() {
    return offer_facility;
  }

  /**
   * @param offer_facility the offer_facility to set
   */
  public void setOffer_facility(String offer_facility) {
    this.offer_facility = offer_facility;
  }

  /**
   * @return the offer_descriptions
   */
  public String getOffer_description() {
    return offer_description;
  }

  /**
   * @param offer_description the offer_descriptions to set
   */
  public void setOffer_description(String offer_description) {
    this.offer_description = offer_description;
  }

  /**
   * @return the offer_group
   */
  public String getOffer_group() {
    return offer_group;
  }

  /**
   * @param offer_group the offer_group to set
   */
  public void setOffer_group(String offer_group) {
    this.offer_group = offer_group;
  }

  /**
   * @return the offer_price
   */
  public double getOffer_price() {
    return offer_price;
  }

  /**
   * @param offer_price the offer_price to set
   */
  public void setOffer_price(double offer_price) {
    this.offer_price = offer_price;
  }

  /**
   * @return the offer_date
   */
  public Timestamp getOffer_date() {
    return offer_date;
  }

  /**
   * @param offer_date the offer_date to set
   */
  public void setOffer_date(Timestamp offer_date) {
    this.offer_date = offer_date;
  }

  /**
   * @return the offer_status
   */
  public boolean isOffer_status() {
    return offer_status;
  }

  /**
   * @param offer_status the offer_status to set
   */
  public void setOffer_status(boolean offer_status) {
    this.offer_status = offer_status;
  }

  /**
   * @return the serialversionuid
   */
  public static long getSerialversionuid() {
    return serialVersionUID;
  }



}
