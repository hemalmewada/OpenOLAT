/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.forum;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.portfolio.ArtefactWizardPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the forum
 * 
 * 
 * Initial date: 01.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumPage {

	private WebDriver browser;
	
	public static final By threadTableBy = By.cssSelector("div.o_sel_forum");
	
	public ForumPage(WebDriver browser) {
		this.browser = browser;
	}

	/**
	 * Get the forum from a course element.
	 * 
	 * @param browser
	 * @return
	 */
	public static ForumPage getCourseForumPage(WebDriver browser) {
		By forumBy = By.cssSelector("div.o_course_run div.o_sel_forum");
		List<WebElement> forumEl = browser.findElements(forumBy);
		Assert.assertFalse(forumEl.isEmpty());
	
		By mainBy = By.cssSelector("div.o_course_run");
		WebElement main = browser.findElement(mainBy);
		Assert.assertTrue(main.isDisplayed());
		return new ForumPage(browser);
	}
	
	public static ForumPage getGroupForumPage(WebDriver browser) {
		By forumBy = By.cssSelector("div.o_sel_forum");
		List<WebElement> forumEl = browser.findElements(forumBy);
		Assert.assertFalse(forumEl.isEmpty());
		return new ForumPage(browser);
	}
	
	public ForumPage clickBack() {
		By backBy = By.cssSelector("a.o_link_back");
		browser.findElement(backBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ForumPage userFilter() {
		By userFilterBy = By.cssSelector("a.o_sel_forum_filter");
		browser.findElement(userFilterBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ForumPage selectFilteredUser(String lastName) {
		By userFilterBy = By.xpath("//table//td//a[text()[contains(.,'" + lastName + "')]]");
		browser.findElement(userFilterBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Create a new thread
	 * 
	 * @param title
	 * @param content
	 * @return
	 */
	public ForumPage createThread(String title, String content, String alias) {
		By newThreadBy = By.className("o_sel_forum_thread_new");
		browser.findElement(newThreadBy).click();
		OOGraphene.waitBusy(browser);
		
		//fill the form
		By titleBy = By.cssSelector("div.modal-content form div.o_sel_forum_message_title input[type='text']");
		OOGraphene.waitElement(titleBy, 5, browser);
		browser.findElement(titleBy).sendKeys(title);
		
		if(alias != null) {
			By aliasBy = By.cssSelector("div.modal-content form div.o_sel_forum_message_alias input[type='text']");
			browser.findElement(aliasBy).sendKeys(alias);
		}
		
		OOGraphene.tinymce(content, browser);
		
		//save
		By saveBy = By.cssSelector("div.modal-content form button.btn-primary");
		WebElement saveButton = browser.findElement(saveBy);
		saveButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ForumPage assertThreadListOnNumber(String thread, int number) {
		By threadBy = By.xpath("//table[contains(@class,'table')]//tr[td[text()='" + number + "']]//a[text()='" + thread + "']");
		browser.findElement(threadBy).click();
		
		return this;
	}
	
	public ForumPage openThread(String title) {
		By threadBy = By.xpath("//table[contains(@class,'table')]//tr//a[text()='" + title + "']");
		OOGraphene.waitElement(threadBy, 5, browser);
		browser.findElement(threadBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ForumPage openThreadInPeekview(String title) {
		By threadBy = By.xpath("//div[contains(@class,'o_forum_peekview_message')]//a[span[text()='" + title + "']]");
		browser.findElement(threadBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ForumPage flatView() {
		By flatBy = By.cssSelector("a.o_forum_all_flat_messages");
		browser.findElement(flatBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ForumPage newMessages() {
		By newBy = By.cssSelector("a.o_forum_new_messages");
		browser.findElement(newBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ForumPage assertOnNewMessages() {
		By messageBodyBy = By.cssSelector("div.o_forum div.o_forum_message_new");
		List<WebElement> messages = browser.findElements(messageBodyBy);
		Assert.assertFalse(messages.isEmpty());
		return this;
	}
	
	public ForumPage assertMessageBody(String text) {
		By messageBodyBy = By.className("o_forum_message_body");
		List<WebElement> messages = browser.findElements(messageBodyBy);
		boolean found = false;
		for(WebElement message:messages) {
			if(message.getText().contains(text)) {
				found = true;
			}
		}
		Assert.assertTrue(found);
		return this;
	}
	
	public ForumPage assertOnGuestPseudonym(String alias) {
		By authorBy = By.xpath("//div[contains(@class,'o_author')][contains(text(),'" + alias + "')]");
		List<WebElement> authorEls = browser.findElements(authorBy);
		Assert.assertFalse(authorEls.isEmpty());
		return this;
	}
	
	public ForumPage waitMessageBody(String text) {
		By messageBy = By.xpath("//div[contains(@class,'o_forum_message_body')][//p[contains(text(),'" + text + "')]]");
		OOGraphene.waitElement(messageBy, 10, browser);
		return this;
	}

	public ForumPage replyToMessage(String reference, String title, String reply) {
		replyToMessageNoWait(reference, title, reply);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ForumPage replyToMessageNoWait(String reference, String title, String reply) {
		By replyBy = By.xpath("//div[contains(@class,'o_forum_message')][//h4[contains(text(),'" + reference + "')]]//a[contains(@class,'o_sel_forum_reply')]");
		browser.findElement(replyBy).click();
		OOGraphene.waitBusy(browser);
		
		if(title != null) {
			By titleBy = By.cssSelector(".o_sel_forum_message_title input[type='text']");
			browser.findElement(titleBy).sendKeys(title);
		}
		OOGraphene.tinymce(reply, browser);
		
		By saveBy = By.cssSelector("fieldset.o_sel_forum_message_form button.btn-primary");
		browser.findElement(saveBy).click();
		return this;
	}
	
	/**
	 * Add the thread to my artefacts
	 * 
	 */
	public ArtefactWizardPage addAsArtfeact() {
		By addAsArtefactBy = By.className("o_eportfolio_add");
		WebElement addAsArtefactButton = browser.findElement(addAsArtefactBy);
		addAsArtefactButton.click();
		OOGraphene.waitBusy(browser);
		return ArtefactWizardPage.getWizard(browser);
	}
}