/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.instantMessaging.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.floatingresizabledialog.FloatingResizableDialogController;
import org.olat.core.gui.themes.Theme;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.iq.AssessmentEvent;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.instantMessaging.CloseInstantMessagingEvent;
import org.olat.instantMessaging.InstantMessageNotification;
import org.olat.instantMessaging.InstantMessagingEvent;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.BuddyStats;
import org.olat.instantMessaging.model.Presence;

/**
 * Description:<br />
 * Main controller which initiates the connection and provides status change/roster and chat possibilities
 * 
 * <P>
 * Initial Date:  26.04.2007 <br />
 * @author guido
 */
public class InstantMessagingMainController extends BasicController implements GenericEventListener {
	
	private static final String ACTION_MSG = "cmd.msg";
	
	private VelocityContainer main = createVelocityContainer("topnav");
	private VelocityContainer chatContent = createVelocityContainer("chat");
	
	//new messages
	private List<Long> showNewMessageHolder = new ArrayList<Long>();
	private VelocityContainer newMsgIcon = createVelocityContainer("newMsgIcon");
	//roster
	private Panel rosterPanel;
	private Link onlineOfflineCount;
	private IMBuddyListController rosterCtr;
	private FloatingResizableDialogController rosterPanelCtr;
	//status changes
	private Panel statusPanel;
	private Link statusChangerLink;
	private IMTopNavStatusController statusChangerCtr;
	private FloatingResizableDialogController statusChangerPanelCtr;
	//chat list
	private JSAndCSSComponent jsc;
	private ChatManagerController chatMgrCtrl;

	private String imStatus;
	private int stateUpdateCounter = 0;
	private boolean inAssessment = false;
	private EventBus singleUserEventCenter;
	private final InstantMessagingService imService;

	public InstantMessagingMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		imService = CoreSpringFactory.getImpl(InstantMessagingService.class);
		
		boolean ajaxOn = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
		chatContent.contextPut("isAjaxMode", Boolean.valueOf(ajaxOn));
		
		//	checks with the given intervall if dirty components are available to rerender
		jsc = new JSAndCSSComponent("intervall", this.getClass(), 5000);
		main.put("updatecontrol", jsc);
		
		// configure new message sound
		newMsgIcon.contextPut("iconsHolder", showNewMessageHolder);
		
		Theme guiTheme = getWindowControl().getWindowBackOffice().getWindow().getGuiTheme();
		String newMessageSoundURL = guiTheme.getBaseURI() + "/sounds/new_message.wav";
		File soundFile = new File(WebappHelper.getContextRoot() + "/themes/" + guiTheme.getIdentifyer() + "/sounds/new_message.wav");
		if (!soundFile.exists()) {
			// fallback to default theme when file does not exist in configured theme
			newMessageSoundURL = newMessageSoundURL.replace("/themes/" + guiTheme.getIdentifyer(), "/themes/openolat");
		}
		newMsgIcon.contextPut("newMessageSoundURL", newMessageSoundURL);
		loadNotifications();

		// status changer link
		statusChangerLink = LinkFactory.createCustomLink("statusChanger", "cmd.status", "", Link.NONTRANSLATED, null, this);
		statusChangerLink.registerForMousePositionEvent(true);
		statusChangerLink.setTooltip(getTranslator().translate("im.status.change.long"));
		updateStatusCss(null);
		main.put("statusChangerPanel", statusChangerLink);

		// roster launcher (offline / online) link
		InstantMessagingModule imModule = CoreSpringFactory.getImpl(InstantMessagingModule.class);
		if (imModule.isGroupPeersEnabled()) {
			onlineOfflineCount = LinkFactory.createCustomLink("onlineOfflineCount", "cmd.roster", "", Link.NONTRANSLATED, main, this);
			onlineOfflineCount.setTooltip(translate("im.roster.intro"));
			onlineOfflineCount.registerForMousePositionEvent(true);
			updateBuddyStats();
			main.put("buddiesSummaryPanel", onlineOfflineCount);
			
			getWindowControl().getWindowBackOffice().addCycleListener(this);
		}
		
		main.put("newMsgPanel", newMsgIcon);
		rosterPanel = new Panel("rosterPanel");
		main.put("rosterPanel", rosterPanel);
		statusPanel = new Panel("statusPanel");
		main.put("statusPanel", statusPanel);
		
		//creates and manages the p2p chats
		chatMgrCtrl = new ChatManagerController(ureq, wControl);
		listenTo(chatMgrCtrl);
		newMsgIcon.put("chats", chatMgrCtrl.getInitialComponent());
		
		//listen to privat chat messages
		imService.listenChat(getIdentity(), getPrivatListenToResourceable(), false, false, this);
		
		singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
		singleUserEventCenter.registerFor(this, getIdentity(), InstantMessagingService.ASSESSMENT_EVENT_ORES);
		singleUserEventCenter.registerFor(this, getIdentity(), InstantMessagingService.TOWER_EVENT_ORES);
		
		putInitialPanel(main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		imService.unlistenChat(getIdentity(), getPrivatListenToResourceable(), this);
		singleUserEventCenter.deregisterFor(this, InstantMessagingService.ASSESSMENT_EVENT_ORES);
		singleUserEventCenter.deregisterFor(this, InstantMessagingService.TOWER_EVENT_ORES);
	}
	
	public OLATResourceable getPrivatListenToResourceable() {
		return OresHelper.createOLATResourceableInstance("Buddy", getIdentity().getKey());	
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == statusChangerLink) {			
			doChangeStatus(ureq);
		} else if (source == onlineOfflineCount) {
			doOpenRoster(ureq);
		} else if (source instanceof Link) {
			Link link = (Link)source;
			//chat gets created by click on buddy list
			if (link.getCommand().equals(ACTION_MSG)) {
				//chats gets created by click on new message icon
				Object obj = link.getUserObject();
				if(obj instanceof Buddy) {
					Buddy buddy = (Buddy)obj;
					chatMgrCtrl.createChat(ureq, buddy);
					showNewMessageHolder.remove(buddy.getIdentityKey());
				}
				newMsgIcon.setDirty(true);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == statusChangerPanelCtr) {
			//closing the floating panel event
			statusPanel.setContent(null);
			removeAsListenerAndDispose(statusChangerCtr);
			removeAsListenerAndDispose(statusChangerPanelCtr);
			statusChangerCtr = null;
			statusChangerPanelCtr = null;
		} else if (source == statusChangerCtr) {
			//update status
			updateStatusCss(statusChangerCtr.getSelectedStatus());
			// remove from UI
			statusChangerPanelCtr.executeCloseCommand();
			statusPanel.setContent(null);
			removeAsListenerAndDispose(statusChangerCtr);
			removeAsListenerAndDispose(statusChangerPanelCtr);
			statusChangerCtr = null;
			statusChangerPanelCtr = null;
		} else if (source == rosterPanelCtr) {
			//closing the floating panel event
			updateBuddyStats();
			removeAsListenerAndDispose(rosterCtr);
			removeAsListenerAndDispose(rosterPanelCtr);
			rosterCtr = null;
			rosterPanelCtr = null;
			rosterPanel.setContent(null);
		} else if (source == rosterCtr) {
			if(event instanceof OpenInstantMessageEvent) {
				OpenInstantMessageEvent e = (OpenInstantMessageEvent)event;
				doOpenPrivateChat(ureq, e.getBuddy()); 
			}
		} else if (source == chatMgrCtrl) {
			//closing events from chat manager controller
		}
	}

	/**
	 * gets called if either a new message or a presence change from one of the buddies happens
	 * or an Assessment starts or ends
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		if(event instanceof InstantMessagingEvent) {
			processInstantMessageEvent((InstantMessagingEvent)event);
		} else if (event instanceof AssessmentEvent) {
			processAssessmentEvent((AssessmentEvent)event);
		} else if (event instanceof OpenInstantMessageEvent) {
			processOpenInstantMessageEvent((OpenInstantMessageEvent)event);
		} else if(event instanceof CloseInstantMessagingEvent) {
			processCloseInstantMessageEvent();
		} else if(Window.BEFORE_INLINE_RENDERING.equals(event)) {
			if(++stateUpdateCounter % 25 == 0) {
				updateBuddyStats();
			}
		}
	}
	
	private void updateBuddyStats() {
		if(onlineOfflineCount != null) {
			BuddyStats stats = imService.getBuddyStats(getIdentity());
			onlineOfflineCount.setCustomDisplayText(translate("im.roster.launch", new String[]{stats.getOnlineBuddies() + "", stats.getOfflineBuddies() + ""}));
		}
	}
	
	private void loadNotifications() {
		List<InstantMessageNotification> notifications = imService.getNotifications(getIdentity());
		for(InstantMessageNotification notification:notifications) {
			if(!showNewMessageHolder.contains(notification.getFromIdentityKey())) {
				showNewMessageHolder.add(notification.getFromIdentityKey());
				Buddy buddy = imService.getBuddyById(notification.getFromIdentityKey());
				createShowNewMessageLink(buddy);
			}
		}
	}
	
	private void doOpenPrivateChat(UserRequest ureq, Buddy buddy) {
		//info.getInitialMessages()
		chatMgrCtrl.createChat(ureq, buddy);
	}
	
	private void doOpenRoster(UserRequest ureq) {
		removeAsListenerAndDispose(rosterCtr);
		removeAsListenerAndDispose(rosterPanelCtr);
		
		rosterCtr = new IMBuddyListController(ureq, getWindowControl());
		listenTo(rosterCtr);
		
		rosterPanelCtr = new FloatingResizableDialogController(ureq, getWindowControl(), rosterCtr.getInitialComponent(),
				translate("im.buddies"), 300, 500, onlineOfflineCount.getOffsetX() - 80, onlineOfflineCount.getOffsetY() + 25,
				null, null, true, true, true, "im_roster"
		);
		listenTo(rosterPanelCtr);
		rosterPanel.setContent(rosterPanelCtr.getInitialComponent());
		onlineOfflineCount.setDirty(false);		
	}
	
	private void doChangeStatus(UserRequest ureq) {
		removeAsListenerAndDispose(statusChangerCtr);
		removeAsListenerAndDispose(statusChangerPanelCtr);
		
		statusChangerCtr = new IMTopNavStatusController(ureq, getWindowControl(), imStatus);
		listenTo(statusChangerCtr);
		
		statusChangerPanelCtr = new FloatingResizableDialogController(ureq, getWindowControl(), statusChangerCtr.getInitialComponent(),
				translate("im.status.change"), 210, 125, statusChangerLink.getOffsetX() - 130, statusChangerLink.getOffsetY() + 25,
				null, null, false, false, true, "im_status");
		listenTo(statusChangerPanelCtr);
		statusPanel.setContent(statusChangerPanelCtr.getInitialComponent());
		statusChangerLink.setDirty(false);
	}
	
	private void updateStatusCss(String status) {
		if(!StringHelper.containsNonWhitespace(status)) {
			imStatus = imService.getStatus(getIdentity().getKey());
		} else {
			imStatus = status;
		}
		if(imStatus == null) {
			imStatus = Presence.available.name();
		  imService.updateStatus(getIdentity(), imStatus);
		}
		String cssClass = "o_instantmessaging_" + imStatus + "_icon";
		statusChangerLink.setCustomEnabledLinkCSS("b_small_icon " + cssClass);
	}
	
	private void processAssessmentEvent(AssessmentEvent event) {
		if(event.getEventType().equals(AssessmentEvent.TYPE.STARTED)) {
			inAssessment = true;
			main.contextPut("inAssessment", true);
			chatMgrCtrl.closeAllChats();
			rosterPanelCtr.executeCloseCommand();
		} else if(event.getEventType().equals(AssessmentEvent.TYPE.STOPPED)) {
			OLATResourceable a = OresHelper.createOLATResourceableType(AssessmentInstance.class);
			if (singleUserEventCenter.getListeningIdentityCntFor(a) < 1) {
				inAssessment = false;
				main.contextPut("inAssessment", false);
				loadNotifications();
			}
		} 
	}

	private void processOpenInstantMessageEvent(OpenInstantMessageEvent event) {
		UserRequest ureq = event.getUserRequest();
		if(ureq != null) {
			if(event.getBuddy() != null) {
				chatMgrCtrl.createChat(ureq, event.getBuddy());
			} else if(event.getOres() != null) {
				//open a group/course chat
				chatMgrCtrl.createGroupChat(ureq, event.getOres(), event.getRoomName(), event.isVip());
			}	
		}
	}
	
	private void processCloseInstantMessageEvent() {
		if(statusChangerPanelCtr != null) {
			statusChangerPanelCtr.executeCloseCommand();
			removeAsListenerAndDispose(statusChangerPanelCtr);
			statusPanel.setContent(null);
		}
		if(rosterPanelCtr != null) {
			rosterPanelCtr.executeCloseCommand();
			removeAsListenerAndDispose(rosterPanelCtr);
			rosterPanel.setContent(null);
		}
		if(chatMgrCtrl != null) {
			chatMgrCtrl.closeAllChats();
		}
	}
	
	private void processInstantMessageEvent(InstantMessagingEvent imEvent) {
		if (imEvent.getCommand().equals("message")) {
			//user receives messages from an other user
			Long fromId = imEvent.getFromId();
			if(!chatMgrCtrl.hasRunningChat(imEvent.getChatResource())) {
				//only show icon if no chat running or msg from other user
				//add follow up message to info holder
				if (!showNewMessageHolder.contains(fromId)) {
					Buddy buddy = imService.getBuddyById(fromId);
					if(Presence.available.name().equals(imStatus) && !inAssessment) {
						doOpenPrivateChat(new SyntheticUserRequest(getIdentity(), getLocale()), buddy);
					} else {
						showNewMessageHolder.add(fromId);
						createShowNewMessageLink(buddy);
					}
				}
			}
		}
	}
	
	/**
	 * creates an new message icon link
	 * @param jabberId
	 */
	private Link createShowNewMessageLink(Buddy buddy) {
		Link link = LinkFactory.createCustomLink(buddy.getIdentityKey().toString(), ACTION_MSG, "", Link.NONTRANSLATED, newMsgIcon, this);
		link.registerForMousePositionEvent(true);
		link.setCustomEnabledLinkCSS("b_small_icon o_instantmessaging_new_msg_icon");
		link.setTooltip(translate("im.new.message", new String[]{ buddy.getName() }));
		link.setUserObject(buddy);
		newMsgIcon.put(buddy.getIdentityKey().toString(), link);
		return link;
	}
}
