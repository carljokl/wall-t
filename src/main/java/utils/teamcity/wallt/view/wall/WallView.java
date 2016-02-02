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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;
import java.util.*;

import static com.google.common.collect.Iterables.size;
import static java.lang.Math.max;

/**
 * Date: 16/02/14
 *
 * @author Cedric Longo
 */
final class WallView extends StackPane {

    public static final int GAP_SPACE = 5;

    private final WallViewModel _model;
    private final Map<Class<?>, WallViewModule.TileViewProvider> _nodeFromModelFactory;

    private Node _currentDisplayedScreen;

    @Inject
    WallView( final WallViewModel model, final Map<Class<?>, WallViewModule.TileViewProvider> nodeFromModelFactory ) {
        _model = model;
        _nodeFromModelFactory = nodeFromModelFactory;
        setStyle( "-fx-background-color:black;" );

        _model.getDisplayedBuilds( ).addListener( (ListChangeListener<TileViewModel>) c -> updateLayout( ) );
        _model.getDisplayedProjects( ).addListener( (ListChangeListener<ProjectTileViewModel>) c -> updateLayout( ) );

        _model.getMaxTilesByColumnProperty( ).addListener( ( o, oldValue, newalue ) -> updateLayout( ) );
        _model.getMaxTilesByRowProperty( ).addListener( ( o, oldValue, newalue ) -> updateLayout( ) );

        _model.isProjectPerWallProperty( ).addListener( ( o, oldValue, newValue ) -> updateLayout() );

        final Timer screenAnimationTimer = new Timer( "WallView Screen switcher", true );
        screenAnimationTimer.scheduleAtFixedRate( new TimerTask( ) {
            @Override
            public void run( ) {
                Platform.runLater( ( ) -> displayNextScreen( ) );
            }
        }, 10000, 10000 );
    }

    private void displayNextScreen( ) {
        if ( getChildren( ).isEmpty( ) )
            return;

        final Node previousScreen = _currentDisplayedScreen;

        final int index = previousScreen == null ? -1 : getChildren( ).indexOf( previousScreen );
        final int nextIndex = ( index == -1 ? 0 : index + 1 ) % getChildren( ).size( );

        final Node nextScreen = getChildren( ).get( nextIndex );

        nextScreen.setVisible( true );
        if ( previousScreen != null && previousScreen != nextScreen )
            previousScreen.setVisible( false );

        _currentDisplayedScreen = nextScreen;
    }

    private void updateLayout( ) {
        getChildren( ).clear( );

        final Collection<TileViewModel> builds = _model.getDisplayedBuilds( );
        final Collection<ProjectTileViewModel> projects = _model.getDisplayedProjects( );

        final int totalTilesCount = builds.size( ) + projects.size( );

        final int maxTilesByColumn = _model.getMaxTilesByColumnProperty( ).get( );
        final int maxTilesByRow = _model.getMaxTilesByRowProperty( ).get( );

        final int maxByScreens = max( 1, maxTilesByColumn * maxTilesByRow );

        final int nbScreen = max( 1, totalTilesCount / maxByScreens + ( ( totalTilesCount % maxByScreens > 0 ? 1 : 0 ) ) );

        int byScreen = max( 1, totalTilesCount / nbScreen + ( ( totalTilesCount % nbScreen > 0 ? 1 : 0 ) ) );
        // We search to complete columns of screen with tiles, not to have empty blanks (ie having a number of column which are all completed)
        while ( byScreen % maxTilesByColumn != 0 )
            byScreen++;

        final int nbColums = max( 1, byScreen / maxTilesByColumn + ( ( byScreen % maxTilesByColumn > 0 ? 1 : 0 ) ) );
        final int byColums = max( 1, byScreen / nbColums + ( ( byScreen % nbColums > 0 ? 1 : 0 ) ) );

        divideByScreen( builds, projects, byScreen, nbColums, byColums );

        displayNextScreen( );
    }

    private void divideByScreen( final Collection<TileViewModel> builds,
                                 final Collection<ProjectTileViewModel> projects,
                                 final int maxTilesPerScreen,
                                 final int nbColumns,
                                 final int byColumns ) {
        if ( _model.isProjectPerWallProperty( ).get( ) ) {
            // Could be optimised but splits into screens by project. Project may span multiple screens.
            final Map<String, Collection<Object>> buildsByProject = Maps.newHashMap();
            for ( TileViewModel build : builds) {
                final String project = build.getBuildTypeData( ).getProjectName( );
                if ( buildsByProject.containsKey( project ) ) {
                    buildsByProject.get( project ).add( build );
                }
                else {
                    final Collection<Object> buildsInProject = Lists.newArrayList( build );
                    buildsByProject.put( project, buildsInProject );
                }
            }
            for ( String project : buildsByProject.keySet() ) {
                final Iterable<List<Object>> projectPartition = Iterables.partition( buildsByProject.get( project ),
                                                                                     maxTilesPerScreen );
                for (List<Object> buildsInScreen : projectPartition) {
                    final GridPane screenPane = buildProjectScreenPane( project, buildsInScreen, nbColumns, byColumns );
                    screenPane.setVisible( false );
                    getChildren( ).add( screenPane );
                }
            }
        }
        else {
            final Iterable<List<Object>> screenPartition = Iterables.partition( Iterables.concat( builds, projects ), maxTilesPerScreen );
            for ( final List<Object> buildsInScreen : screenPartition ) {
                final GridPane screenPane = buildScreenPane( buildsInScreen, nbColumns, byColumns );
                screenPane.setVisible( false );
                getChildren( ).add( screenPane );
            }
        }
    }


    private GridPane buildCommonScreenPane( ) {
        final GridPane screenPane = new GridPane( );
        screenPane.setHgap( GAP_SPACE );
        screenPane.setVgap( GAP_SPACE );
        screenPane.setPadding( new Insets( GAP_SPACE ) );
        screenPane.setStyle( "-fx-background-color:black;" );
        screenPane.setAlignment( Pos.CENTER );
        return screenPane;
    }

    private GridPane buildScreenPane( final Iterable<Object> buildsInScreen, final int nbColums, final int byColums ) {
        final GridPane screenPane = buildCommonScreenPane();
        final Iterable<List<Object>> partition = Iterables.paddedPartition( buildsInScreen, byColums );
        for ( int x = 0; x < nbColums; x++ ) {
            final List<Object> buildList = x < size( partition ) ? Iterables.get( partition, x ) : Collections.emptyList( );
            for ( int y = 0; y < byColums; y++ ) {
                if ( buildList.isEmpty( ) ) {
                    createEmptyTile( screenPane, x, y, nbColums, byColums );
                    continue;
                }

                final Object build = Iterables.get( buildList, y );
                if ( build == null )
                    createEmptyTile( screenPane, x, y, nbColums, byColums );
                else
                    createTileFromModel( screenPane, build, x, y, nbColums, byColums );
            }
        }

        return screenPane;
    }

    private GridPane buildProjectScreenPane( final String projectName, final Iterable<Object> buildsInScreen, final int nbColums, final int byColums ) {
        final GridPane screenPane = buildCommonScreenPane();

        final Iterable<List<Object>> partition = Iterables.paddedPartition( buildsInScreen, byColums );
        createTitleBar( screenPane, projectName, nbColums, byColums + 1 );
        for ( int x = 0; x < nbColums; x++ ) {
            final List<Object> buildList = x < size( partition ) ? Iterables.get( partition, x ) : Collections.emptyList( );
            for ( int y = 0; y < byColums; y++ ) {
                if ( buildList.isEmpty( ) ) {
                    createEmptyTile( screenPane, x, y + 1, nbColums, byColums + 1 );
                    continue;
                }

                final Object build = Iterables.get( buildList, y );
                if ( build == null )
                    createEmptyTile( screenPane, x, y + 1, nbColums, byColums + 1 );
                else
                    createTileFromModel( screenPane, build, x, y + 1, nbColums, byColums + 1 );
            }
        }

        return screenPane;
    }

    private void createTitleBar( final GridPane screenPane, final String title, final int nbColumns, final int nbRows ) {
        final Pane tile = new TitleView( title, _model.getViewConfig() );
        tile.prefWidthProperty( ).bind( widthProperty( ).add( -( ( nbColumns + 1 ) * GAP_SPACE ) / nbColumns ) );
        tile.prefHeightProperty( ).bind( heightProperty( ).add( -( nbRows + 1 ) * GAP_SPACE ).divide( nbRows ) );
        tile.setMinSize( USE_PREF_SIZE, USE_PREF_SIZE );
        tile.setMaxSize( USE_PREF_SIZE, USE_PREF_SIZE );
        screenPane.add( tile, 0, 0, nbColumns, 1 );
    }

    private void createEmptyTile( final GridPane screenPane, final int x, final int y, final int nbColumns, final int nbRows ) {
        final Pane tile = new Pane( );
        tile.prefWidthProperty( ).bind( widthProperty( ).add( -( nbColumns + 1 ) * GAP_SPACE ).divide( nbColumns ) );
        tile.prefHeightProperty( ).bind( heightProperty( ).add( -( nbRows + 1 ) * GAP_SPACE ).divide( nbRows ) );
        tile.setMinSize( USE_PREF_SIZE, USE_PREF_SIZE );
        tile.setMaxSize( USE_PREF_SIZE, USE_PREF_SIZE );
        screenPane.add( tile, x, y );
    }

    private void createTileFromModel( final GridPane screenPane, final Object model, final int x, final int y, final int nbColumns, final int nbRows ) {
        final Pane tile = _nodeFromModelFactory.get( model.getClass( ) ).get( model );
        tile.prefWidthProperty( ).bind( widthProperty( ).add( -( nbColumns + 1 ) * GAP_SPACE ).divide( nbColumns ) );
        tile.prefHeightProperty( ).bind( heightProperty( ).add( -( nbRows + 1 ) * GAP_SPACE ).divide( nbRows ) );
        tile.setMinSize( USE_PREF_SIZE, USE_PREF_SIZE );
        tile.setMaxSize( USE_PREF_SIZE, USE_PREF_SIZE );
        screenPane.add( tile, x, y );
    }


}
