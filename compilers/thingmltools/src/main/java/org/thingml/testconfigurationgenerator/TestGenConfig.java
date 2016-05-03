/**
 * Copyright (C) 2014 SINTEF <franck.fleurey@sintef.no>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingml.testconfigurationgenerator;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sintef
 */
public class TestGenConfig {
    static public Set<Language> getLanguages(File outputDir, String options) {
        Set<Language> languages = new HashSet<>();

        Language javascript = new Language(outputDir, "Nodejs", "nodejs");
        Language posix = new Language(outputDir, "Posix", "posix");
        Language java = new Language(outputDir, "Java", "java");
        Language arduino = new Language(outputDir, "Arduino", "arduino");
        Language sintefboard = new Language(outputDir, "Sintefboard", "sintefboard");

        if (options == null) {
            options = "all";
        }

        if ((options.compareToIgnoreCase(javascript.longName) == 0) || (options.compareToIgnoreCase("all") == 0)) {
            languages.add(javascript);
        }
        if ((options.compareToIgnoreCase(posix.longName) == 0) || (options.compareToIgnoreCase("all") == 0)) {
            languages.add(posix);
        }
        if ((options.compareToIgnoreCase(java.longName) == 0) || (options.compareToIgnoreCase("all") == 0)) {
            languages.add(java);
        }
        if ((options.compareToIgnoreCase(arduino.longName) == 0) || (options.compareToIgnoreCase("all") == 0)) {
            languages.add(arduino);
        }
        if ((options.compareToIgnoreCase(sintefboard.longName) == 0) || (options.compareToIgnoreCase("all") == 0)) {
            languages.add(sintefboard);
        }
        //languages.add(arduino);

        return languages;
    }
}
