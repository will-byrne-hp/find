package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.query.*;
import com.hp.autonomy.frontend.selenium.element.DatePicker;
import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Date;
import java.util.List;

public class FindPage extends AppElement implements AppPage,
        IndexFilter.Filterable,
        DatePickerFilter.Filterable,
        StringDateFilter.Filterable,
        ParametricFilter.Filterable {

    private final FindResultsPage results;

    FindPage(WebDriver driver){
        super(new WebDriverWait(driver,30)
                .withMessage("loading Find page")
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid"))),driver);
        results = new FindResultsPage(driver);
    }

    protected FilterPanel filters() {
        return new FilterPanel(new IndexesTree.Factory(), getDriver());
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid")));
    }

    public void unhover() {
        /* click somewhere not important to remove hover -
        * clicking the user's username seems safe... */
        getDriver().findElement(By.className("user-username")).click();
        new WebDriverWait(getDriver(), 2).until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("popover"))));
    }

    /**
     * waits until the list of indexes has been retrieved
     * from HOD if necessary
     */
    void waitForIndexes() {
        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.invisibilityOfElementLocated(By.className("not-loading")));
    }

    public FindResultsPage getResultsPage() {
        return results;
    }

    @Override
    public IndexesTree indexesTree() {
        return filters().indexesTree();
    }

    public void sortBy(SortBy sortBy) {
        sortDropdown().select(sortBy.toString());
    }

    private Dropdown sortDropdown() {
        WebElement dropdownContainer = findElement(By.cssSelector(".sort-container"));
        return new Dropdown(dropdownContainer, getDriver());
    }

    @Override
    public void filterBy(QueryFilter filter) {
        filter.apply(this);
        results.waitForResultsToLoad();
    }

    @Override
    public DatePicker fromDatePicker() {
        return filters().datePickerFilterable().fromDatePicker();
    }

    @Override
    public DatePicker untilDatePicker() {
        return filters().datePickerFilterable().untilDatePicker();
    }

    @Override
    public FormInput fromDateInput() {
        return filters().stringDateFilterable().fromDateInput();
    }

    @Override
    public FormInput untilDateInput() {
        return filters().stringDateFilterable().untilDateInput();
    }

    @Override
    public String formatInputDate(Date date) {
        return filters().stringDateFilterable().formatInputDate(date);
    }

    @Override
    public WebElement parametricContainer() {
        WebElement firstParametric = findElement(By.cssSelector("[data-field]"));
        return ElementUtil.ancestor(firstParametric, 2);
    }

    @Override
    public void waitForParametricValuesToLoad() {
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.invisibilityOfElementLocated(By.className("parametric-processing-indicator")));
    }

    // this can be used to check whether on the landing page,
    // as opposed to main results page
    public WebElement footerLogo() {
        return findElement(By.className("hp-logo-footer"));
    }

    public Boolean loadingIndicatorExists(){return findElements(By.className("view-server-loading-indicator")).size()>0;}

    public WebElement loadingIndicator(){
        return findElement(By.className("view-server-loading-indicator"));
    }

    public WebElement previewContents(){
        return findElement(By.className("preview-mode-contents"));
    }

    public int totalResultsNum(){return Integer.parseInt(findElement(By.className("total-results-number")).getText());}

    public void openDetailedPreview(){
        findElement(By.className("preview-mode-open-detail-button")).click();
    }

    WebElement leftContainer(){
        return findElement(By.cssSelector(".left-side-container"));
    }

    public List<String> getFilterLabels() {
        return ElementUtil.getTexts(findElements(By.className("filter-label")));
    }

    public void scrollToBottom() {
        findElement(By.className("results-number")).click();
        DriverUtil.scrollToBottom(getDriver());
        results.waitForResultsToLoad();
    }

    public static class Factory implements ParametrizedFactory<WebDriver, FindPage> {
        public FindPage create(WebDriver context) {
            return new FindPage(context);
        }
    }
}
