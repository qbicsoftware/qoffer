/*******************************************************************************
 * QBiC Offer Generator provides an infrastructure for creating offers using QBiC portal and
 * infrastructure. Copyright (C) 2018 Benjamin Sailer
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

package life.qbic.utils;

import java.io.*;
import java.util.ArrayList;

/**
 * Helper class to parse the csv file holding the package discount per sample size.
 */
public class CsvParserUtils {

  public static ArrayList<Float> parseCsvFile(String filename, String csvSplitBy,
      boolean skipHeader) {

    ArrayList<Float> parsedCsvFile = new ArrayList<>();
    // add a placeholder for the discount to skip the first value, so we can easily access the
    // discount via
    // arrayList[5] -> discount for 5 samples
    parsedCsvFile.add(-1.0f);

    try {
      BufferedReader br = new BufferedReader(new FileReader(filename));
      try {
        if (skipHeader)
          br.readLine();
        String line;
        while ((line = br.readLine()) != null) {
          String[] lineSplitted = line.split(csvSplitBy);
          parsedCsvFile.add(Float.parseFloat(lineSplitted[1])); //
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          br.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return parsedCsvFile;
  }
}
