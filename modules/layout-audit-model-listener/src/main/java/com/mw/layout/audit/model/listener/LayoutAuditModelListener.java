package com.mw.layout.audit.model.listener;

import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.model.CTPreferences;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.change.tracking.service.CTPreferencesLocalService;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.audit.event.generators.constants.EventTypes;
import com.liferay.portal.security.audit.event.generators.util.AuditMessageBuilder;

import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = ModelListener.class)
public class LayoutAuditModelListener extends BaseModelListener<Layout> {


	@Activate
	protected void activate(Map<String, Object> properties) {
		_log.info("Activating...");
	}
	
	@Override
	public void onAfterRemove(Layout model) throws ModelListenerException {
		
		try {
			long currentCtCollectionId = -1;
			
			PermissionChecker permissionChecker = PermissionThreadLocal.getPermissionChecker();
			
			if (permissionChecker != null && permissionChecker.getUser() != null) {
				currentCtCollectionId = getCTCollectionId(permissionChecker.getCompanyId(), permissionChecker.getUser().getUserId());
			}
			
			String notes = "";
			
			if (model.getType().equalsIgnoreCase(LayoutConstants.TYPE_CONTENT)) {
				if (model.getClassPK() > 0) {
					notes += "Deleted page is the Edit version of the Content Page.";
				} else {
					notes += "Deleted page is the View version of the Content Page.";
				}
			}
			
			if (currentCtCollectionId > -1) {
				if (Validator.isNotNull(notes)) notes += ", ";
				notes += "Deleted page was deleted from within publication with ctCollectionId: " + currentCtCollectionId + ".";
			}
						
			AuditMessage auditMessage = AuditMessageBuilder.buildAuditMessage(EventTypes.DELETE, Layout.class.getName(), model.getPlid(), null);
	
			JSONObject additionalInfoJSONObject = auditMessage.getAdditionalInfo();
	
			additionalInfoJSONObject.
				put("plId:", model.getPlid()).	
				put("layoutId", model.getLayoutId()).	
				put("groupId", model.getGroupId()).
				put("groupName", model.getGroup().getName(Locale.UK)).
				put("name", model.getName(Locale.UK)).
				put("title", model.getName(Locale.UK)).
				put("friendlyURL", model.getFriendlyURL(Locale.UK)).
				put("classPK", model.getClassPK()).
				put("hidden", model.isHidden()).
				put("system", model.isSystem()).
				put("draftLayout", model.isDraftLayout()).
				put("draft", model.isDraft()).
				put("approved", model.isApproved()).
				put("privateLayout", model.isPrivateLayout()).
				put("type", model.getType()).
				put("ctCollection", model.getCtCollectionId()).
				put("uuid", model.getUuid()).
				put("notes", notes);
			
			_log.info(additionalInfoJSONObject.toString());
	
			_auditRouter.route(auditMessage);

		} catch (Exception exception) {
			_log.error(exception);
			
			throw new ModelListenerException(exception);
    	}
	}	
	
	private long getCTCollectionId(long companyId, long userId) {	
		long ctCollectionId = -1;
		
		try {
			CTCollection ctCollection = null;
			
			CTPreferences ctPreferences = _ctPreferencesLocalService.fetchCTPreferences(companyId, userId);
			
			if (ctPreferences == null) {
				return ctCollectionId;
			}
	
			ctCollection = _ctCollectionLocalService.fetchCTCollection(ctPreferences.getCtCollectionId());

			if (ctCollection != null) {
				ctCollectionId = ctCollection.getCtCollectionId();
			}
		} catch (Exception e) {
			_log.error("Exception retrieving ctCollectionId, assume Production. " + e.getMessage(), e);
		}

		return ctCollectionId;
	}		
	
	@Reference
	private AuditRouter _auditRouter;
	
	@Reference
	private CTPreferencesLocalService _ctPreferencesLocalService;
	
	@Reference
	private CTCollectionLocalService _ctCollectionLocalService;
	
	private static Log _log = LogFactoryUtil.getLog(LayoutAuditModelListener.class);
}