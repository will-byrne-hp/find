package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.FindTopNavBar;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.categories.CoreFeature;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.CommonMatchers.containsAnyOf;
import static com.hp.autonomy.frontend.selenium.matchers.CommonMatchers.containsItems;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.*;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

@RelatedTo("CSA-2091")
public class RelatedConceptsITCase extends FindTestBase {
    private FindService findService;
    private FindResultsPage results;
    private FindTopNavBar navBar;


    public RelatedConceptsITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findService = getApplication().findService();
        results = getElementFactory().getResultsPage();
        navBar = getElementFactory().getTopNavBar();
    }

    @Test
    public void testRelatedConceptsHasResults(){
        findService.search("Danye West");
        for (WebElement concept : results.relatedConcepts()) {
            assertThat(concept, hasTextThat(not(isEmptyOrNullString())));
            assertThat(concept, not(containsTextIgnoringCase("loading")));
        }
    }

    @Test
    @RelatedTo("CCUK-3598")
    public void testRelatedConceptsNavigateOnClick(){
        String search = "Red";
        findService.search(search);
        WebElement topRelatedConcept = results.relatedConcepts().get(0);
        String concept = topRelatedConcept.getText();

        topRelatedConcept.click();
        assertThat(navBar.getAlsoSearchingForTerms(), hasItem(concept));
        assertThat(navBar.getSearchBoxTerm(), is(search));
    }

    @Test
    @KnownBug({"CCUK-3498", "CSA-2066"})
    public void testRelatedConceptsHover(){
        findService.search("Find");
        WebElement popover = results.hoverOverRelatedConcept(0);
        verifyThat(popover, hasTextThat(not(isEmptyOrNullString())));
        verifyThat(popover.getText(), not(containsString("QueryText-Placeholder")));
        verifyThat(popover.getText(), not(containsString(Errors.Search.RELATED_CONCEPTS)));
        results.unhover();
    }

    @Test
    public void testRelatedConceptsInResults(){
        findService.search("Tiger");
        results.highlightRelatedConceptsButton().click();

        for(WebElement relatedConceptLink : results.relatedConcepts()) {
            String relatedConcept = relatedConceptLink.getText();
            for (WebElement relatedConceptElement : results.highlightedSausages(relatedConcept)) {
                if (relatedConceptElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
                    verifyThat(relatedConceptElement, containsTextIgnoringCase(relatedConcept));
                }
                verifyThat(relatedConceptElement, hasTagName("a"));
                verifyThat(relatedConceptElement, hasClass("clickable"));
            }
        }
    }

    @Test
    @RelatedTo("CCUK-3599")
    public void testRelatedConceptsHighlightButton() {
        findService.search("pancakes");
        WebElement button = results.highlightRelatedConceptsButton();

        verifyThat(results.highlightedSausages(""), empty());
        verifyThat(button, not(hasClass("active")));

        button.click();
        verifyThat(results.highlightedSausages(""), not(empty()));
        verifyThat(button, hasClass("active"));

        button.click();
        verifyThat(results.highlightedSausages(""), empty());
        verifyThat(button, not(hasClass("active")));
    }

    @Test
    public void testMultipleAdditionalConcepts() {
        findService.search("bongo");

        List<String> relatedConcepts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String newConcept = clickFirstNewConcept(relatedConcepts);
            verifyThat(navBar.getAlsoSearchingForTerms(), hasItem(newConcept));
        }
        verifyThat(navBar.getSearchBoxTerm(), is("bongo"));
        verifyThat(navBar.getAlsoSearchingForTerms(), hasSize(relatedConcepts.size()));
        verifyThat(navBar.getAlsoSearchingForTerms(), containsItems(relatedConcepts));
    }

    @Test
    @Category(CoreFeature.class)
    public void testAddRemoveConcepts() {
        findService.search("jungle");
        List<String> concepts = new ArrayList<>();
        String firstConcept = clickFirstNewConcept(concepts);
        String secondConcept = clickFirstNewConcept(concepts);
        verifyThat(navBar.getAlsoSearchingForTerms(), hasSize(2));

        navBar.additionalConcept(firstConcept).removeAndWait();
        List<String> alsoSearchingFor = navBar.getAlsoSearchingForTerms();

        verifyThat(alsoSearchingFor, hasSize(1));
        verifyThat(alsoSearchingFor, not(hasItem(firstConcept)));
        verifyThat(alsoSearchingFor, hasItem(secondConcept));
        verifyThat(navBar.getSearchBoxTerm(), is("jungle"));
    }

    @Test
    @KnownBug("CCUK-3566")
    public void testTermNotInRelatedConcepts() {
        final String query = "world cup";
        findService.search(query);
        List<String> addedConcepts = new ArrayList<>();

        verifyThat(results.getRelatedConcepts(), not(hasItem(equalToIgnoringCase(query))));

        for (int i = 0; i < 5; i++) {
            clickFirstNewConcept(addedConcepts);
            verifyThat(results.getRelatedConcepts(), not(hasItem(equalToIgnoringCase(query))));
        }
    }

    @Test
    @KnownBug("CCUK-3566")
    public void testAdditionalConceptsNotAlsoRelated() {
        findService.search("matt");
        List<String> addedConcepts = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            clickFirstNewConcept(addedConcepts);
            verifyThat(results.getRelatedConcepts(), not(containsAnyOf(addedConcepts)));
        }
    }

    @Test
    @KnownBug("CCUK-3706")
    public void testAddSausageToQuery() {
        findService.search("sausage");
        results.highlightRelatedConceptsButton().click();

        List<String> addedConcepts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            WebElement firstSausage = results.highlightedSausages("").get(0);
            LOGGER.info("clicking sausage " + firstSausage.getText());
            addedConcepts.add(firstSausage.getText());
            firstSausage.click();

            verifyThat(navBar.getSearchBoxTerm(), is("sausage"));
            verifyThat(navBar.getAlsoSearchingForTerms(), containsItems(addedConcepts));
        }
    }

    @Test
    public void testRefreshAddedConcepts() {
        findService.search("fresh");
        List<String> concepts = new ArrayList<>();
        clickFirstNewConcept(concepts);
        clickFirstNewConcept(concepts);

        getWindow().refresh();
        navBar = getElementFactory().getTopNavBar();

        verifyThat(navBar.getSearchBoxTerm(), is("fresh"));
        verifyThat(navBar.getAlsoSearchingForTerms(), containsItems(concepts));
    }

    private String clickFirstNewConcept(List<String> existingConcepts) {
        for (WebElement concept : results.relatedConcepts()) {
            String conceptText = concept.getText();
            if (!existingConcepts.contains(conceptText)) {
                LOGGER.info("clicking concept " + conceptText);
                concept.click();
                existingConcepts.add(conceptText);
                results.unhover();
                return conceptText;
            }
        }
        throw new NoSuchElementException("no new related concepts");
    }
}
