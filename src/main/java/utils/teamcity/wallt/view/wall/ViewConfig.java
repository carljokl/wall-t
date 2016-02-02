/*******************************************************************************
 * Copyright 2014 Cedric Longo.
 *
 * This file is part of Wall-T program.
 *
 * Wall-T is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Wall-T is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Wall-T.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package utils.teamcity.wallt.view.wall;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.text.FontWeight;

/**
 * Object grouping view configuration that can be customised
 *
 * @author Carl Jokl
 * @since 01/02/2016.
 */
public class ViewConfig {

    private final IntegerProperty _fontSize;
    private final ObjectProperty<FontWeight> _fontWeight;

    /**
     * Create an instance of view configuration set to default values.
     */
    public ViewConfig() {
        _fontSize = new SimpleIntegerProperty( 50 );
        _fontWeight = new SimpleObjectProperty<FontWeight>( FontWeight.BOLD );
    }

    /**
     * Create a new instance of view configuration with the specified values.
     *
     * @param fontSize The font size of the UI item.
     * @param fontWeight The font weight of the UI item.
     */
    public ViewConfig(final int fontSize,
                      final FontWeight fontWeight) {
        _fontSize = new SimpleIntegerProperty( fontSize );
        _fontWeight = new SimpleObjectProperty<FontWeight>( fontWeight);
    }


    public IntegerProperty fontSize() {
        return _fontSize;
    }

    public ObjectProperty<FontWeight> fontWeight() {
        return _fontWeight;
    }

}
