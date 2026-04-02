package com.mw.layout.audit.model.listener;

import com.liferay.change.tracking.configuration.CTSettingsConfiguration;
import com.liferay.change.tracking.constants.CTConstants;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.model.CTPreferences;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.change.tracking.service.CTPreferencesLocalService;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
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
			String notes = "";
						
			AuditMessage auditMessage = AuditMessageBuilder.buildAuditMessage(EventTypes.DELETE, Layout.class.getName(), model.getPlid(), null);
			
			boolean publicationsEnabled = isPublicationsEnabled(auditMessage.getCompanyId());
			
			if (Validator.isNotNull(model.getType())) {
				if (model.getType().equalsIgnoreCase(LayoutConstants.TYPE_ASSET_DISPLAY)) {
					if (Validator.isNotNull(notes)) notes += " ";
					notes += "Deleted page was a Display Page Template.";
				}
				
				if (model.getType().equalsIgnoreCase(LayoutConstants.TYPE_CONTENT) || model.getType().equalsIgnoreCase(LayoutConstants.TYPE_ASSET_DISPLAY)) {
					if (model.getClassPK() > 0) {
						if (Validator.isNotNull(notes)) notes += " ";
						notes += "Deleted page was the Edit version of the page.";
					} else {
						if (Validator.isNotNull(notes)) notes += " ";
						notes += "Deleted page was the View version of the page.";
					}
				}	
			}
			
			JSONObject additionalInfoJSONObject = auditMessage.getAdditionalInfo();
			
			if (publicationsEnabled) {
				CollectionModel collectionModel = getCollectionModel(auditMessage.getCompanyId(), auditMessage.getUserId());

				if (collectionModel != null && !collectionModel.isProduction()) {
					if (Validator.isNotNull(notes)) notes += " ";
					notes += "Page was deleted from within " + collectionModel.getName() + " Publication, ctCollectionId: " + collectionModel.getCtCollectionId() + ".";

					additionalInfoJSONObject.put("selectedCtCollectionId", model.getCtCollectionId());
					additionalInfoJSONObject.put("selectedCtCollectionName", collectionModel.getName());
				} else {
					if (Validator.isNotNull(notes)) notes += " ";
					notes += "Page was deleted from within Publications Production mode.";
				}				
			}
				
			additionalInfoJSONObject.
				put("plId:", model.getPlid()).
				put("layoutId", model.getLayoutId()).
				put("primaryKey", model.getPrimaryKey()).
				put("classPK", model.getClassPK()).
				put("masterLayoutPlid", model.getMasterLayoutPlid()).
				put("typeSettings", model.getTypeSettings()).
				put("groupId", model.getGroupId()).
				put("groupName", model.getGroup().getName(locale)).
				put("name", model.getName(locale)).
				put("title", model.getName(locale)).
				put("friendlyURL", model.getFriendlyURL(locale)).
				put("type", model.getType()).
				put("privateLayout", model.isPrivateLayout()).
				put("hidden", model.isHidden()).
				put("system", model.isSystem()).
				put("draftLayout", model.isDraftLayout()).
				put("draft", model.isDraft()).
				put("approved", model.isApproved()).
				put("ctCollectionId", model.getCtCollectionId()).
				put("mvccVersion", model.getMvccVersion()).
				put("uuid", model.getUuid());
			
			if (Validator.isNotNull(notes)) additionalInfoJSONObject.put("notes", notes);
			
			_log.info(additionalInfoJSONObject.toString());
	
			_auditRouter.route(auditMessage);

		} catch (Exception e) {
			_log.error("Exception processing onAfterRemove. " + e.getMessage(), e);
    	}
	}	
	
	private CollectionModel getCollectionModel(long companyId, long userId) {
		try {
			CTCollection ctCollection = null;
			CTPreferences ctPreferences = _ctPreferencesLocalService.fetchCTPreferences(companyId, userId);
			
			if (ctPreferences == null) return null; // Means in Production...
						
			if (ctPreferences.getCtCollectionId() == CTConstants.CT_COLLECTION_ID_PRODUCTION) {
				return new CollectionModel(CTConstants.CT_COLLECTION_ID_PRODUCTION, "Production", true);
			}
			
			ctCollection = _ctCollectionLocalService.fetchCTCollection(ctPreferences.getCtCollectionId());
			
			if (ctCollection != null) {
				return new CollectionModel(ctCollection.getCtCollectionId(), ctCollection.getName(), ctCollection.isProduction());
			}
			
			return null;
		} catch (Exception e) {
			_log.error("Exception retrieving CTCollection. " + e.getMessage(), e);
		}

		return null;
	}
	
	private boolean isPublicationsEnabled(long companyId) {
		CTSettingsConfiguration configuration = null;
		
		try {
			configuration = _configurationProvider.getCompanyConfiguration(CTSettingsConfiguration.class, companyId);
		} catch (Exception e) {
			_log.error("Exception retrieving CTSettingsConfiguration. " + e.getMessage(), e);
		}
		
		if (configuration != null) {
			return configuration.enabled();
		}
		
		return false;
	}
	
	@Reference
	private AuditRouter _auditRouter;
	
	@Reference
	private CTPreferencesLocalService _ctPreferencesLocalService;
	
	@Reference
	private CTCollectionLocalService _ctCollectionLocalService;
	
	@Reference
	private ConfigurationProvider _configurationProvider;
	
	private Locale locale = Locale.US;
	
	private static Log _log = LogFactoryUtil.getLog(LayoutAuditModelListener.class);
}