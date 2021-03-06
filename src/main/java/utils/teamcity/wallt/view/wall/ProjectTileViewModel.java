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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import com.google.inject.assistedinject.Assisted;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.layout.Background;
import utils.teamcity.wallt.model.build.BuildStatus;
import utils.teamcity.wallt.model.build.IProjectManager;
import utils.teamcity.wallt.model.build.ProjectData;
import utils.teamcity.wallt.model.configuration.Configuration;

import javax.inject.Inject;
import java.util.Set;

/**
 * Date: 22/02/14
 *
 * @author Cedric Longo
 */
final class ProjectTileViewModel {

    private final IProjectManager _projectManager;
    private final ProjectData _projectData;

    private final StringProperty _displayedName = new SimpleStringProperty( );
    private final ObjectProperty<Background> _background = new SimpleObjectProperty<>( );

    private final IntegerProperty _successCount = new SimpleIntegerProperty( -1 );
    private final IntegerProperty _failureCount = new SimpleIntegerProperty( -1 );

    private final BooleanProperty _hasSuccessRunning = new SimpleBooleanProperty( );
    private final BooleanProperty _hasFailureRunning = new SimpleBooleanProperty( );

    private final BooleanProperty _lightMode = new SimpleBooleanProperty( );
    private final ViewConfig _viewConfig;


    interface Factory {
        ProjectTileViewModel forProjectData( final ViewConfig viewConfig, final ProjectData projectData );
    }

    @Inject
    ProjectTileViewModel( final Configuration configuration, final IProjectManager projectManager, @Assisted final ViewConfig viewConfig, @Assisted final ProjectData projectData ) {
        _projectManager = projectManager;
        _projectData = projectData;
        _viewConfig = viewConfig;
        updateConfiguration( configuration );
        updateProjectViewModel( projectData );
    }

    public ViewConfig getViewConfig() {
        return _viewConfig;
    }

    @Subscribe
    public final void updateProjectViewModel( final ProjectData data ) {
        final Set<ProjectData> allProjects = getAllInterestingProjects( );

        if ( !allProjects.contains( data ) )
            return;

        Platform.runLater( ( ) -> {
            _displayedName.set( Strings.isNullOrEmpty( _projectData.getAliasName( ) ) ? _projectData.getName( ) : _projectData.getAliasName( ) );
            updateSuccessFailureCount( );
            updateBackground( );
        } );
    }

    private void updateSuccessFailureCount( ) {
        final Set<ProjectData> allProjects = getAllInterestingProjects( );
        _failureCount.setValue( allProjects.stream( ).map( p -> p.getBuildTypeCount( BuildStatus.FAILURE, BuildStatus.ERROR ) ).reduce( 0, Integer::sum ) );
        _successCount.setValue( allProjects.stream( ).map( p -> p.getBuildTypeCount( BuildStatus.SUCCESS ) ).reduce( 0, Integer::sum ) );

        _hasFailureRunning.set( allProjects.stream( ).anyMatch( p -> p.hasBuildTypeRunning( BuildStatus.FAILURE, BuildStatus.ERROR ) ) );
        _hasSuccessRunning.set( allProjects.stream( ).anyMatch( p -> p.hasBuildTypeRunning( BuildStatus.SUCCESS ) ) );
    }

    @Subscribe
    public void updateConfiguration( final Configuration configuration ) {
        Platform.runLater( ( ) -> {
            _lightMode.setValue( configuration.isLightMode( ) );
        } );
    }

    private void updateBackground( ) {
        if ( getFailureCount( ) + getSuccessCount( ) == 0 ) {
            _background.setValue( BuildBackground.UNKNOWN.getMain( ) );
            return;
        }
        // Setting main background according to failure count
        _background.setValue( getFailureCount( ) == 0 ? BuildBackground.SUCCESS.getMain( ) : BuildBackground.FAILURE.getMain( ) );
    }


    private Set<ProjectData> getAllInterestingProjects( ) {
        return ImmutableSet.<ProjectData>builder( )
                .add( _projectData )
                .addAll( _projectManager.getAllChildrenOf( _projectData ) )
                .build( );
    }

    String getDisplayedName( ) {
        return _displayedName.get( );
    }

    StringProperty displayedNameProperty( ) {
        return _displayedName;
    }

    Background getBackground( ) {
        return _background.get( );
    }

    ObjectProperty<Background> backgroundProperty( ) {
        return _background;
    }

    BooleanProperty lightModeProperty( ) {
        return _lightMode;
    }

    public boolean isLightMode( ) {
        return _lightMode.get( );
    }

    int getSuccessCount( ) {
        return _successCount.get( );
    }

    IntegerProperty successCountProperty( ) {
        return _successCount;
    }

    int getFailureCount( ) {
        return _failureCount.get( );
    }

    IntegerProperty failureCountProperty( ) {
        return _failureCount;
    }

    boolean hasSuccessRunning( ) {
        return _hasSuccessRunning.get( );
    }

    BooleanProperty hasSuccessRunningProperty( ) {
        return _hasSuccessRunning;
    }

    boolean hasFailureRunning( ) {
        return _hasFailureRunning.get( );
    }

    BooleanProperty hasFailureRunningProperty( ) {
        return _hasFailureRunning;
    }


}
