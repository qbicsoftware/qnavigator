package de.uni_tuebingen.qbic.qbicmainportlet;


import helpers.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import logging.Log4j2Logger;
import model.ExperimentBean;
import model.ProjectBean;
import model.SearchResultsExperimentBean;
import model.SearchResultsSampleBean;
import model.SpaceBean;

import org.tepi.filtertable.FilterTable;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.WebBrowser;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class SearchResultsView extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9100320125534037596L;

	/**
	 * 
	 */

	public final static String navigateToLabel = "searchresults";

	private logging.Logger LOGGER = new Log4j2Logger(SearchResultsView.class);

	String caption;
	FilterTable table;

	DataHandler datahandler;

	VerticalLayout searchResultsLayout;

	BeanItemContainer<SearchResultsSampleBean> sampleBeanContainer;
	BeanItemContainer<SearchResultsExperimentBean> expBeanContainer;

	String queryString = new String();
	Grid sampleGrid = new Grid();
	Grid expGrid = new Grid();
	//Boolean includePatientCreation = false;
	ToolBar toolBar;
	State state;
	String resourceUrl;
	String header;

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}


	private Button export = new Button("Export as TSV");

	private int numberOfProjects = 0;

	private String user;

	public SearchResultsView(DataHandler datahandler, String caption, String user, State state, String resUrl) {
		searchResultsLayout = new VerticalLayout();
		this.table = buildFilterTable();
		this.datahandler = datahandler;

		this.state = state;
		this.resourceUrl = resUrl;

		this.user = user;



	}



	public void setSizeFull() {
		searchResultsLayout.setSizeFull();
		super.setSizeFull();
		this.table.setSizeFull();
		searchResultsLayout.setSpacing(true);
		//homeview_content.setMargin(true);
	}

	/**
	 * sets the ContainerDataSource of this view. Should usually contains project information. Caption
	 * is caption.
	 * 
	 * @param homeViewInformation
	 * @param caption
	 */
	public void setContainerDataSource(String caption) {

		this.caption = caption;
		//		this.currentBean = spaceBean;
		//		this.numberOfProjects = currentBean.getProjects().size();
		//
		//		setExportButton();
		//
		//		this.table.setContainerDataSource(spaceBean.getProjects());
		//		this.table.setVisibleColumns(new Object[] {"code", "space", "description"});
		//		this.table.setColumnHeader("code", "Name");
		//		this.table.setColumnHeader("space", "Project");
		//		this.table.setColumnHeader("description", "Description");
		//		this.table.setColumnExpandRatio("Name", 1);
		//		this.table.setColumnExpandRatio("Description", 3);


		List<Sample> sampleResults = datahandler.getSampleResults();
		List<Experiment> expResults = datahandler.getExpResults();
		queryString = datahandler.getLastQueryString();


		Collection<SearchResultsExperimentBean> expCollection = new ArrayList<SearchResultsExperimentBean>();

		SearchResultsSampleBean tmpSearchBean;
		SearchResultsExperimentBean tmpExperimentBean;

		for (Experiment i : expResults) {
			//System.out.println(i);
			//Label sampleLabel = new Label(i.toString());
			//SearchResultsSampleItem sampleItem = new SearchResultsSampleItem(i, rowNumber);

			tmpExperimentBean = new SearchResultsExperimentBean(i, queryString);
			expCollection.add(tmpExperimentBean);

		}

		expBeanContainer = new BeanItemContainer<SearchResultsExperimentBean>(SearchResultsExperimentBean.class, expCollection);

		Collection<SearchResultsSampleBean> sampleCollection = new ArrayList<SearchResultsSampleBean>();

		for (Sample i : sampleResults) {
			//System.out.println(i);
			//Label sampleLabel = new Label(i.toString());
			//SearchResultsSampleItem sampleItem = new SearchResultsSampleItem(i, rowNumber);

			tmpSearchBean = new SearchResultsSampleBean(i, queryString);
			sampleCollection.add(tmpSearchBean);

		}

		sampleBeanContainer = new BeanItemContainer<SearchResultsSampleBean>(SearchResultsSampleBean.class, sampleCollection);

	}

	private void setExportButton() {
		//		buttonLayoutSection.removeAllComponents();
		//		HorizontalLayout buttonLayout = new HorizontalLayout();
		//		buttonLayout.setHeight(null);
		//		buttonLayout.setWidth("100%");
		//		buttonLayoutSection.addComponent(buttonLayout);
		//
		//		buttonLayout.addComponent(this.export);
		//
		//		StreamResource sr =
		//				Utils.getTSVStream(Utils.containerToString(currentBean.getProjects()), this.caption);
		//		FileDownloader fileDownloader = new FileDownloader(sr);
		//		fileDownloader.extend(this.export);
	}

	/**
	 * updates view, if height, width or the browser changes.
	 * 
	 * @param browserHeight
	 * @param browserWidth
	 * @param browser
	 */
	public void updateView(int browserHeight, int browserWidth, WebBrowser browser) {
		setWidth((browserWidth * 0.85f), Unit.PIXELS);
	}

	/**
	 * 
	 * @return
	 */
	ToolBar initToolBar() {
		//SearchBarView searchBarView = new SearchBarView(datahandler);
		SearchEngineView searchEngineView = new SearchEngineView(datahandler);

		toolBar = new ToolBar(resourceUrl, state, searchEngineView);
		toolBar.init();
		return toolBar;
	}

	/**
	 * updates the menu bar based on the new content (currentbean was changed)
	 */
	void updateContentToolBar() {
		toolBar.setDownload(false);
		toolBar.setWorkflow(false);
		toolBar.update("", "");
	}

	void buildLayout(int browserHeight, int browserWidth, WebBrowser browser) {
		this.setMargin(new MarginInfo(false,true,false,false));
		// clean up first
		searchResultsLayout.removeAllComponents();
		searchResultsLayout.setWidth("100%");
		searchResultsLayout.setSpacing(true);
		
		Label header = new Label("Search results for query '" + queryString + "':");
		searchResultsLayout.addComponent(header);

		updateView(browserWidth, browserWidth, browser);

		VerticalLayout viewContent = new VerticalLayout();
		viewContent.setWidth("100%");
		viewContent.setSpacing(true);

		//expGrid = new Grid(expBeanContainer);
		expGrid = new Grid(expBeanContainer);
		expGrid.setCaption("Found Experiments");
		expGrid.setColumnOrder("experimentID", "experimentName", "matchedField");
		expGrid.setSizeFull();
		
		expGrid.getColumn("experimentID").setExpandRatio(0);
		expGrid.getColumn("experimentName").setExpandRatio(1);
		expGrid.getColumn("matchedField").setExpandRatio(1);

		expGrid.setHeightMode(HeightMode.ROW);
		expGrid.setHeightByRows(5);
		expGrid.setSelectionMode(SelectionMode.SINGLE);

		



		expGrid.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				System.out.println(event.getItemId() + " ::: " + event.getPropertyId()) ;
				String cellType = new String(event.getPropertyId().toString());

				if (cellType.equals("experimentID")) {
					String cellContent = new String(expBeanContainer.getContainerProperty(event.getItemId(),
							event.getPropertyId()).getValue().toString());

					Notification.show("Loading experiment " + cellContent);

					State state = (State) UI.getCurrent().getSession()
							.getAttribute("state");
					ArrayList<String> message = new ArrayList<String>();
					message.add("clicked");
					message.add(cellContent);
					message.add("experiment");
					state.notifyObservers(message);
				}
			}
		});

		if (expBeanContainer.size() == 0) {
			Label noExps = new Label("no experiments were found");
			noExps.setCaption("Found Experiments");
			viewContent.addComponent(noExps);
		}
		else {
			viewContent.addComponent(expGrid);
		}



		sampleGrid = new Grid(sampleBeanContainer);
		sampleGrid.setCaption("Found Samples");
		sampleGrid.setColumnOrder("sampleID", "sampleName", "matchedField");
		sampleGrid.setSizeFull();
		
		sampleGrid.getColumn("sampleID").setExpandRatio(0);
		sampleGrid.getColumn("sampleName").setExpandRatio(1);
		sampleGrid.getColumn("matchedField").setExpandRatio(1);

		sampleGrid.setHeightMode(HeightMode.ROW);
		sampleGrid.setHeightByRows(5);
		sampleGrid.setSelectionMode(SelectionMode.SINGLE);

		sampleGrid.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				System.out.println(event.getItemId() + " ::: " + event.getPropertyId()) ;
				String cellType = new String(event.getPropertyId().toString());

				if (cellType.equals("sampleID")) {
					String cellContent = new String(sampleBeanContainer.getContainerProperty(event.getItemId(),
							event.getPropertyId()).getValue().toString());

					Notification.show("Loading sample " + cellContent);

					State state = (State) UI.getCurrent().getSession()
							.getAttribute("state");
					ArrayList<String> message = new ArrayList<String>();
					message.add("clicked");
					message.add(cellContent);
					message.add("sample");
					state.notifyObservers(message);
				}
			}
		});

		if (sampleBeanContainer.size() == 0) {
			Label noSamples = new Label("no samples were found");
			noSamples.setCaption("Found Samples");
			viewContent.addComponent(noSamples);
		}
		else {
			viewContent.addComponent(sampleGrid);
		}

		searchResultsLayout.addComponent(viewContent);
		
		this.addComponent(searchResultsLayout);
	}

	
	private void updateUI()
	{
		expGrid.setContainerDataSource(expBeanContainer);
		sampleGrid.setContainerDataSource(sampleBeanContainer);
		
	}
	
	private void tableClickChangeTreeView() {
		table.setSelectable(true);
		table.setImmediate(true);
		this.table.addValueChangeListener(new ViewTablesClickListener(table, ProjectView.navigateToLabel));
	}


	private FilterTable buildFilterTable() {
		FilterTable filterTable = new FilterTable();

		filterTable.setFilterDecorator(new DatasetViewFilterDecorator());
		filterTable.setFilterGenerator(new DatasetViewFilterGenerator());

		filterTable.setFilterBarVisible(true);

		filterTable.setSelectable(true);
		filterTable.setImmediate(true);

		filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

		filterTable.setColumnCollapsingAllowed(false);

		filterTable.setColumnReorderingAllowed(true);
		return filterTable;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		setContainerDataSource("test");
		int height = event.getNavigator().getUI().getPage().getBrowserWindowHeight();
		int width = event.getNavigator().getUI().getPage().getBrowserWindowWidth();
		buildLayout(height, width, event.getNavigator().getUI().getPage().getWebBrowser());
	}

	/**
	 * Enables or disables the component. The user can not interact disabled components, which are
	 * shown with a style that indicates the status, usually shaded in light gray color. Components
	 * are enabled by default.
	 */
	public void setEnabled(boolean enabled) {
		this.export.setEnabled(enabled);
		this.table.setEnabled(enabled);
	}


}
