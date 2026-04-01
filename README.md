## Introduction ##
- This is a Model Listener for the Layout model (i.e. Pages) that creates an Audit Event when a Page is deleted from the system.

## Sample Audit Event > Additional Information ##
```
{"notes":"Deleted page is the Edit version of the Content Page., Deleted page was deleted from within publication with ctCollectionId: 1.",
"hidden":true,
"groupId":"32099",
"draftLayout":true,
"title":"zzzzzz",
"privateLayout":false,
"type":"content",
"layoutId":"8",
"uuid":"8149b713-37ed-12b9-ffc4-e4962fea0545",
"groupName":"MW",
"classPK":"24",
"approved":true,
"system":true,
"ctCollection":"1",
"draft":false,
"name":"zzzzzz",
"friendlyURL":"/80b381e5-e5b6-31b6-7bb3-5f68616cc8b9",
"plId:":"25"}
```

## Implementation Notes ##
- Content Pages have a 'Read' and an 'Edit' version of the Page. When a Content Page is deleted each will trigger the Model Listener. A note is included in the Audit Event:
```
Deleted page is the View version of the Content Page.
```
or
```
Deleted page is the Edit version of the Content Page.
```
- When a Page is deleted within a Publication the Model Listener is triggered at that point. It is not triggered again when the Publication is Published. A note is included when the Page is deleted from within a Publication:
```
 Deleted page was deleted from within publication with ctCollectionId: 999.
```

## Notes ##
- This is a ‘proof of concept’ that is being provided ‘as is’ without any support coverage or warranty.
- The implementation uses a custom OSGi module meaning it is compatible with Liferay DXP Self-Hosted and Liferay PaaS, but is not compatible with Liferay SaaS.
- The implementation was tested locally using Liferay DXP 2024.Q1.10.
- JDK 11 is expected for both compile time and runtime.
