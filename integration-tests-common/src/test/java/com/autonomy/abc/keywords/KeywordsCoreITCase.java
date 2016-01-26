package com.autonomy.abc.keywords;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.categories.CoreFeature;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.*;

public class KeywordsCoreITCase extends ABCTestBase {
    private KeywordService keywordService;
    private SearchService searchService;

    public KeywordsCoreITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        keywordService = getApplication().createKeywordService(getElementFactory());
        searchService = getApplication().createSearchService(getElementFactory());
    }

    @After
    public void tearDown() {
        keywordService.deleteAll(KeywordFilter.ALL);
    }

    @Test
    @Category(CoreFeature.class)
    public void testCreateBlacklist() {
        final String blacklist = "naughty";
        keywordService.addBlacklistTerms(blacklist);
        SearchPage searchPage = searchService.search(blacklist);
        assertThat(searchPage.visibleDocumentsCount(), is(0));
    }

    @Test
    @Category(CoreFeature.class)
    public void testCreateSynonyms() {
        final String hasResults = "car";
        final String noResults = "zxpqw";
        keywordService.addSynonymGroup(hasResults, noResults);
        SearchPage searchPage = searchService.search(noResults);
        assertThat(searchPage.visibleDocumentsCount(), greaterThan(0));
    }

    @Test
    @Category(CoreFeature.class)
    public void testDeleteKeyword() {
        final String keyword = "asdfghjkl";
        keywordService.addBlacklistTerms(keyword);
        keywordService.deleteKeyword(keyword);
        assertThat(getElementFactory().getKeywordsPage(), not(containsText(keyword)));
    }
}
