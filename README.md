## Introduction ##
- This is a Model Listener for the Layout model (i.e. Pages) that creates an Audit Event when a Page is deleted from the system.

## Sample Audit Event > Additional Information ##
```
{
"notes":"Deleted page was the View version of the page. Page was deleted from within Publications Production mode.",
"hidden":false,
"groupId":"32099",
"typeSettings":"published=false\n","draftLayout":false,"title":"AAAAA",
"type":"content",
"privateLayout":false,
"layoutId":"39",
"uuid":"3e9dd7da-a895-f7fb-d42a-67b75003f022",
"classPK":"0",
"groupName":"MW",
"approved":true,
"system":false,
"draft":false,
"name":"AAAAA",
"masterLayoutPlid":"0",
"ctCollectionId":"0",
"friendlyURL":"/aaaaa",
"mvccVersion":"8",
"plId:":"62",
"primaryKey":"62"
}
```

## Implementation Notes ##
- The Layout model represents Static Pages, Utility Pages and Templates such as Master Page Templates, Page Templates and Display Page Templates.
- Additional entities such as LayoutPrototype, LayoutUtilityPageEntry and LayoutPageTemplateEntry are used to manage the Utility Pages and different Templates types.
	- However since the Layout is being deleted we can't rely on these records being present to determine if a specific Page is a Utility Page or a type of Template.
- Content Pages (and Templates based on Content Pages) have a 'Read' and an 'Edit' version of the Page. When a Content Page is deleted each will trigger the Model Listener. A note is included in the Audit Event:
```
Deleted page was the View version of the page.
```
or
```
Deleted page was the Edit version of the page.
```
- When a Page is deleted within a Publication, the Model Listener is triggered at that point. It is not triggered again when the Publication is Published. A note is included when the Page is deleted from within a Publication:
```
Page was deleted from within Christmas Promotion Publication, ctCollectionId: 1345.
```

## Notes ##
- This is a ‘proof of concept’ that is being provided ‘as is’ without any support coverage or warranty.
- Test it in non-production before considering deploying it in a production environment. 
- The implementation uses a custom OSGi module meaning it is compatible with Liferay DXP Self-Hosted and Liferay PaaS, but is not compatible with Liferay SaaS.
- The implementation was tested locally using Liferay DXP 2024.Q1.10 with Publications enabled.
- JDK 11 is expected for both compile time and runtime.
- Consider deactivating the module when doing a bulk Page delete or when deleting a Site or a Virtual Instance.