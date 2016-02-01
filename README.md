# Dynamo
The Accelerator Framework is a software development framework developed by Open Circle Solutions that aims to increase productivity by using design principles such as convention over configuration, model-driven development and DRY (don’t repeat yourself).
At the core of the Accelerator Framework is the concept of the Entity Model. The Entity Model is defined as the model describes the attributes and behaviour of an entity (or domain object) in your application. This entity model can then be used as the basis for creating forms, tables, search screens etc. 
The Entity Model of an entity can be automatically generated based on the properties of the attribute model (using sensible defaults as described by the convention over configuration principle) and can further be modified by using attributes and message bundle overwrites. The main goal is to reduce the amount of (boilerplate) code required to perform common actions like creating search screens and edit forms. 
Complementing the Entity Model is a set of user interface components (widgets) that can be used to quickly construct screens for common use cases, and a number of base classes for the Data Access and Service layers. 
The Accelerator Framework is built around a number of proven and highly productive set of technologies:
•	JPA2 for ORM
•	QueryDSL for type-safe query generation
•	Spring as the application framework
•	Vaadin as the user interface framework
•	Apache Camel for integration

