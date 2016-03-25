package com.ocs.dynamo.showcase.movies;

import javax.inject.Inject;

import com.ocs.dynamo.showcase.Views;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.layout.SimpleSearchLayout;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.view.BaseView;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.VerticalLayout;

@SpringView(name = Views.MOVIES_VIEW)
@UIScope
@SuppressWarnings("serial")
public class MoviesView extends BaseView {

    private VerticalLayout mainLayout;

    @Inject
    private MovieService movieService;

    public void enter(ViewChangeEvent event) {

        mainLayout = new VerticalLayout();

        VerticalLayout main = new DefaultVerticalLayout(true, true);
        main.addComponent(mainLayout);

        FormOptions fo = new FormOptions();
        fo.setShowRemoveButton(true);

        SimpleSearchLayout<Integer, Movie> movieLayout = new SimpleSearchLayout<Integer, Movie>(
                movieService, getModelFactory().getModel(Movie.class), QueryType.ID_BASED,
                new FormOptions(),
                new com.vaadin.data.sort.SortOrder("id", SortDirection.ASCENDING)) {
        };

        mainLayout.addComponent(movieLayout);
        setCompositionRoot(main);
    }
}
