package de.uni_tuebingen.qbic.qbicmainportlet;

import helpers.UglyToPrettyNameMapper;
import helpers.Utils;

import java.util.ArrayList;

import logging.Log4j2Logger;
import logging.Logger;
import model.ExperimentBean;

import org.tepi.filtertable.FilterTable;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

public class ExperimentView extends VerticalLayout implements View {

  /**
   * 
   */
  private static final long serialVersionUID = -9156593640161721690L;
  static Logger LOGGER = new Log4j2Logger(ExperimentView.class);
  public final static String navigateToLabel = "experiment";
  FilterTable table;
  VerticalLayout expview_content;

  private Button export;
  private DataHandler datahandler;
  private State state;
  private String resourceUrl;
  private VerticalLayout buttonLayoutSection;
  private FileDownloader fileDownloader;
  private ExperimentBean currentBean;
  private ToolBar toolbar;
  private MenuItem downloadCompleteProjectMenuItem;
  private MenuItem datasetOverviewMenuItem;
  private MenuItem createBarcodesMenuItem;
  private Label generalInfoLabel;
  private Label statContentLabel;
  private Label propertiesContentLabel;

  private UglyToPrettyNameMapper prettyNameMapper = new UglyToPrettyNameMapper();
  private TabSheet expview_tab;

  public ExperimentView(DataHandler datahandler, State state, String resourceurl) {
    this(datahandler, state);
    this.resourceUrl = resourceurl;
  }


  public ExperimentView(DataHandler datahandler, State state) {
    this.datahandler = datahandler;
    this.state = state;
    resourceUrl = "javascript;";
    initView();
  }


  /**
   * updates view, if height, width or the browser changes.
   * 
   * @param browserHeight
   * @param browserWidth
   * @param browser
   */
  public void updateView(int browserHeight, int browserWidth, WebBrowser browser) {
    setWidth((browserWidth * 0.6f), Unit.PIXELS);
  }

  /**
   * init this view. builds the layout skeleton Menubar Description and others Statisitcs Experiment
   * Table Graph
   */
  void initView() {

    expview_content = new VerticalLayout();
    expview_tab = new TabSheet();
    expview_tab.addStyleName(ValoTheme.TABSHEET_ICONS_ON_TOP);
    expview_tab.addStyleName(ValoTheme.TABSHEET_CENTERED_TABS);
    
    expview_content.addComponent(initToolBar());
    expview_content.addComponent(expview_tab);
    
    expview_tab.addTab(initDescription(), "General Information").setIcon(FontAwesome.INFO);;
    expview_tab.addTab(initStatistics(), "Statistics").setIcon(FontAwesome.BAR_CHART_O);
    expview_tab.addTab(initProperties(), "Metadata").setIcon(FontAwesome.LIST_UL);
    expview_tab.addTab(initTable(), "Samples").setIcon(FontAwesome.FLASK);;

    //expview_content.addComponent(initDescription());
    //expview_content.addComponent(initStatistics());
    //expview_content.addComponent(initTable());
    //expview_content.addComponent(initButtonLayout());

    // use the component that is returned by initTable
    // projectview_content.setComponentAlignment(this.table, Alignment.TOP_CENTER);
    expview_content.setWidth("100%");
    this.addComponent(expview_content);
  }

  /**
   * This function should be called each time currentBean is changed
   */
  public void updateContent() {
    updateContentToolBar();
    updateContentDescription();
    updateContentStatistics();
    updateContentProperties();
    updateContentTable();
    updateContentButtonLayout();
  }

  /**
   * 
   * @return
   */
  HorizontalLayout initButtonLayout() {
    this.export = new Button("Export as TSV");
    buttonLayoutSection = new VerticalLayout();
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setMargin(new MarginInfo(false, false, false, true));
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayoutSection.setSpacing(true);
    buttonLayoutSection.addComponent(buttonLayout);
    buttonLayoutSection.setMargin(new MarginInfo(false, false, false, true));
    buttonLayout.addComponent(this.export);
    return buttonLayout;
  }

  void updateContentButtonLayout() {
    if (fileDownloader != null)
      this.export.removeExtension(fileDownloader);
    StreamResource sr =
        Utils.getTSVStream(Utils.containerToString(currentBean.getSamples()), currentBean.getId());
    fileDownloader = new FileDownloader(sr);
    fileDownloader.extend(this.export);
  }

  /**
   * 
   * @return
   */
  ToolBar initToolBar() {
    SearchBarView searchBarView = new SearchBarView(datahandler);
    toolbar = new ToolBar(resourceUrl, state, searchBarView);
    toolbar.init();
    return toolbar;
  }

  /**
   * updates the menu bar based on the new content (currentbean was changed)
   */
  void updateContentToolBar() {

    Boolean containsData = currentBean.getContainsData();
    toolbar.setDownload(containsData);
    toolbar.setWorkflow(containsData);
    toolbar.update(navigateToLabel, currentBean.getId());
  }


  /**
   * initializes the description layout
   * 
   * @return
   */
  VerticalLayout initDescription() {
    VerticalLayout generalInfo = new VerticalLayout();
    VerticalLayout generalInfoContent = new VerticalLayout();
    //generalInfoContent.setCaption("General Information");
    //generalInfoContent.setIcon(FontAwesome.INFO);
    generalInfoLabel = new Label("");

    generalInfo.setMargin(new MarginInfo(true, false, false, true));
    generalInfoContent.addComponent(generalInfoLabel);
    generalInfoContent.setMargin(new MarginInfo(true, false, false, true));
    //generalInfoContent.setMargin(true);
    //generalInfo.setMargin(true);

    generalInfo.addComponent(generalInfoContent);

    return generalInfo;
  }

  void updateContentDescription() {
    generalInfoLabel.setValue(String.format("Stage:\t %s", prettyNameMapper.getPrettyName(currentBean.getType())));

  }

  /**
   * 
   * @return
   * 
   */
  VerticalLayout initStatistics() {
    VerticalLayout statistics = new VerticalLayout();

    HorizontalLayout statContent = new HorizontalLayout();
    //statContent.setCaption("Statistics");
    //statContent.setIcon(FontAwesome.BAR_CHART_O);


    // int numberOfDatasets = dh.datasetMap.get(experimentBean.getId()).size();
    statContentLabel = new Label("");

    statContent.addComponent(statContentLabel);
    statContent.setMargin(new MarginInfo(true, false, false, true));

    // statContent.addComponent(new Label(String.format("%s dataset(s).",numberOfDatasets )));
    //statContent.setMargin(true);
    //statContent.setMargin(new MarginInfo(false, false, false, true));
    //statContent.setSpacing(true);

    /*
     * if (numberOfDatasets > 0) {
     * 
     * String lastSample = "No samples available"; if (experimentBean.getLastChangedSample() !=
     * null) { lastSample = experimentBean.getLastChangedSample();// .split("/")[2]; }
     * statContent.addComponent(new Label(String.format( "Last change %s",
     * String.format("occurred in sample %s (%s)", lastSample,
     * experimentBean.getLastChangedDataset().toString())))); }
     */


    statistics.addComponent(statContent);
    //statistics.setMargin(true);

    // Properties of experiment
    //VerticalLayout properties = new VerticalLayout();
    //VerticalLayout propertiesContent = new VerticalLayout();
    //propertiesContent.setCaption("Properties");
    //propertiesContent.setIcon(FontAwesome.LIST_UL);
    //propertiesContentLabel = new Label("", ContentMode.HTML);
    //propertiesContent.addComponent(propertiesContentLabel);
    //properties.addComponent(propertiesContent);
    //propertiesContent.setMargin(new MarginInfo(true, false, false, true));

    //properties.setMargin(true);
    //statistics.addComponent(properties);

    statistics.setMargin(new MarginInfo(true, false, false, true));
    statistics.setSpacing(true);

    return statistics;
  }
  
  /**
   * 
   */
  void updateContentStatistics() {
    statContentLabel.setValue(String.format("%s sample(s),", currentBean.getSamples().size()));
  }
  
  VerticalLayout initProperties() {
    // Properties of experiment
    VerticalLayout properties = new VerticalLayout();
    VerticalLayout propertiesContent = new VerticalLayout();
    //propertiesContent.setCaption("Properties");
    //propertiesContent.setIcon(FontAwesome.LIST_UL);
    propertiesContentLabel = new Label("", ContentMode.HTML);
    propertiesContent.addComponent(propertiesContentLabel);
    properties.addComponent(propertiesContent);
    propertiesContent.setMargin(new MarginInfo(true, false, false, true));
    
    return properties;
  }

   void updateContentProperties() {
     propertiesContentLabel.setValue(currentBean.generatePropertiesFormattedString());
   }

  VerticalLayout initTable() {
    this.table = this.buildFilterTable();
    this.tableClickChangeTreeView();
    VerticalLayout tableSection = new VerticalLayout();
    HorizontalLayout tableSectionContent = new HorizontalLayout();
    //tableSectionContent.setCaption("Registered Samples");
    //tableSectionContent.setIcon(FontAwesome.FLASK);
    tableSectionContent.addComponent(this.table);
    tableSectionContent.setMargin(new MarginInfo(true, false, false, true));

    //tableSectionContent.setMargin(true);
    //tableSection.setMargin(true);
    tableSection.setMargin(new MarginInfo(true, false, false, true));

    this.table.setWidth("100%");
    tableSection.setWidth("100%");
    tableSectionContent.setWidth("100%");

    tableSection.addComponent(tableSectionContent);
    
    this.export = new Button("Export as TSV");
    buttonLayoutSection = new VerticalLayout();
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setMargin(new MarginInfo(false, false, false, true));
    buttonLayout.setHeight(null);
    buttonLayout.setWidth("100%");
    buttonLayoutSection.setSpacing(true);
    buttonLayoutSection.addComponent(buttonLayout);
    buttonLayoutSection.setMargin(new MarginInfo(false, false, false, false));
    buttonLayout.addComponent(this.export);
    
    tableSection.addComponent(buttonLayoutSection);

    return tableSection;
  }


  void updateContentTable() {
    // Nothing to do here at the moment
    // table is already set in setdataresource
  }

  public void setResourceUrl(String resourceurl) {
    this.resourceUrl = resourceurl;
  }

  public String getResourceUrl() {
    return resourceUrl;
  }

  public String getNavigatorLabel() {
    return navigateToLabel;
  }

  /**
   * sets the ContainerDataSource for showing it in a table and the id of the current Openbis
   * Experiment. The id is shown in the caption.
   * 
   * @param projectInformation
   * @param id
   */
  public void setContainerDataSource(ExperimentBean experimentBean) {
    this.currentBean = experimentBean;
    LOGGER.debug(String.valueOf(experimentBean.getSamples().size()));
    this.table.setContainerDataSource(experimentBean.getSamples());
    this.table.setVisibleColumns(new Object[] {"code", "type"});

    int rowNumber = this.table.size();

    if (rowNumber == 0) {
      this.table.setVisible(false);
    }
    else {
      this.table.setVisible(true);
      this.table.setPageLength(Math.min(rowNumber, 10));
    }
    


  }

  private void tableClickChangeTreeView() {
    table.setSelectable(true);
    table.setImmediate(true);
    this.table.addValueChangeListener(new ViewTablesClickListener(table, SampleView.navigateToLabel));
  }

  private FilterTable buildFilterTable() {
    FilterTable filterTable = new FilterTable();
    filterTable.setSizeFull();

    filterTable.setFilterDecorator(new DatasetViewFilterDecorator());
    filterTable.setFilterGenerator(new DatasetViewFilterGenerator());

    filterTable.setFilterBarVisible(true);

    filterTable.setSelectable(true);
    filterTable.setImmediate(true);

    filterTable.setRowHeaderMode(RowHeaderMode.INDEX);

    filterTable.setColumnCollapsingAllowed(true);

    filterTable.setColumnReorderingAllowed(true);

    filterTable.setColumnHeader("code", "QBiC ID");
    filterTable.setColumnHeader("type", "Sample Type");

    return filterTable;
  }


  @Override
  public void enter(ViewChangeEvent event) {
    String currentValue = event.getParameters();
    LOGGER.debug(currentValue);
    // TODO updateContent only if currentExperiment is not equal to newExperiment
    this.table.unselect(this.table.getValue());
    this.setContainerDataSource(datahandler.getExperiment2(currentValue));

    updateContent();
  }


  public ExperimentBean getCurrentBean() {
    return currentBean;
  }

  /**
   * Enables or disables the component. The user can not interact disabled components, which are
   * shown with a style that indicates the status, usually shaded in light gray color. Components
   * are enabled by default.
   */
  public void setEnabled(boolean enabled) {
    this.export.setEnabled(enabled);
    this.table.setEnabled(enabled);
    // this.createBarcodesMenuItem.getParent().setEnabled(false);
    // this.downloadCompleteProjectMenuItem.getParent().setEnabled(false);
    this.toolbar.setEnabled(enabled);
  }


}
