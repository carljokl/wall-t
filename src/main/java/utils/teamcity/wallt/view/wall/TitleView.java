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

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import utils.teamcity.wallt.view.UIUtils;

import static javafx.geometry.Pos.CENTER_LEFT;

/**
 * This view is intended to display just a title piece of text
 *
 * @author Carl Jokl
 * @since 02/02/2016.
 */
public class TitleView extends StackPane {

    public TitleView( final String title, ViewConfig config ) {

        setAlignment( CENTER_LEFT );
        setStyle( "-fx-border-color:white; -fx-border-radius:5;" );
        final HBox tileContent = new HBox( );
        tileContent.setAlignment( CENTER_LEFT );

        final Label tileTitle = new Label( title );
        tileTitle.setFont( UIUtils.font( config.fontSize( ).get( ), config.fontWeight( ).get( ) ) );
        tileTitle.setTextFill( Color.WHITE );
        tileTitle.setPadding( new Insets( 5 ) );
        tileTitle.setWrapText( true );
        tileTitle.setEffect( UIUtils.shadowEffect() );
        HBox.setHgrow( tileTitle, Priority.SOMETIMES );
        tileContent.getChildren( ).add( tileTitle );
        getChildren().addAll( tileContent );
    }
}
