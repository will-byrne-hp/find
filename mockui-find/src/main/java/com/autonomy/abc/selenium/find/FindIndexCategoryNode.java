package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.NodeElement;
import com.autonomy.abc.selenium.indexes.tree.NodeElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

class FindIndexCategoryNode extends IndexCategoryNode {
    private WebElement container;
    private WebDriver driver;

    FindIndexCategoryNode(WebElement clickable, WebDriver webDriver) {
        super(new FindIndexLeafNode(clickable, webDriver), clickable, webDriver);
        container = clickable;
        driver = webDriver;
    }

    @Override
    public List<NodeElement> getIndexNodes() {
        List<NodeElement> nodes = new ArrayList<>();
        for (WebElement element : container.findElements(By.cssSelector(".clickable[data-name]"))) {
            nodes.add(new FindIndexLeafNode(element, driver));
        }
        return nodes;
    }

    @Override
    public NodeElement find(String name) {
        WebElement childElement = container.findElement(By.cssSelector(".clickable[data-name='" + name+"']"));
        return new FindIndexLeafNode(childElement, driver);
    }

    @Override
    public IndexCategoryNode findCategory(String name) {
        WebElement childElement = container.findElement(By.cssSelector(".clickable[data-category-id='" + name.toLowerCase() + "']"));
        return new FindIndexCategoryNode(childElement, driver);
    }

    @Override
    public String getName() {
        return container.findElement(By.className("category-name")).getText();
    }
}
