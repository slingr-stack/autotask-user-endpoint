---
title: Autotask user endpoint
keywords: 
last_updated: August 28, 2017
tags: []
summary: "Detailed description of the API of the Autotask user endpoint."
sidebar: extensions_sidebar
permalink: endpoints_autotask_user.html
folder: extensions
---

## Overview

The Autotask user endpoint allows to create, read, update and delete elements in Autotask. It is exactly the
same as the regular [Autotask]({{site.baseurl}}/endpoints_autotask.html) endpoint, but it supports one Autotask 
account per user.

Some of the features are:

- CRUD operations on any entity based
- Automatic detection of zone
- Conversion of XML to JSON and the other way around 
- Automatic conversion of fields based on fields definition on Autotask
- Polling to detect new records and updates in existing records

Please refer to the [Autotask]({{site.baseurl}}/endpoints_autotask.html) endpoint for detailed documentation. In this 
page we will just clarify what's different.

## Configuration

### Polling enabled

Indicates if polling will be enabled to detect new records or updates in existing records.

### Polling frequency

How often (in minutes) polling will be done. It must be 1 or greater.

### Entities to poll

This is a comma-separated list of entities to poll. For example:

```
Account,Ticket,Task
```

Keep in mind that not all entities can be polled and some can only be polled to detect creations. Here is the list:

- Create and update: Account, AccountNote, AccountToDo, Contact, ContractNote, Phase, ProjectNote, Service, 
  ServiceBundle, ServiceCall, Task, TaskNote, Ticket, TicketNote, TimeEntry
- Only create: Appointment, ContractCost, ContractMilestone, InstalledProduct, Invoice, Opportunity, Project, 
  ProjectCost, PurchaseOrder, Quote, QuoteTemplate, TicketCost

## User configuration

You will need to create a user in Autotask that will be used to access the API. All requests to the API will be done
on behalf of that user.

### Username

The username of the user to make request to the API.

### Password

The password of the user to make request to the API.

## Javascript API

The Javascript API is exactly the same as the one in the [Autotask]({{site.baseurl}}/endpoints_autotask.html) endpoint. 

## Events

Events are exactly the same as the ones in the [Autotask]({{site.baseurl}}/endpoints_autotask.html) endpoint.
 
Just keep in mind that you will get events for each user that is connected to the endpoint. In the listener processing
the event the current user will be set to the one associated to the Autotask account that generated the event.

## About SLINGR

SLINGR is a low-code rapid application development platform that accelerates development, with robust architecture for integrations and executing custom workflows and automation.

[More info about SLINGR](https://slingr.io)

## License

This endpoint is licensed under the Apache License 2.0. See the `LICENSE` file for more details.
