/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.vocabulary;

import org.apache.jena.rdf.model.* ;

/**
The standard Activity Stream vocabulary.
*/

public class AS {

  /**
   * The namespace of the vocabulary as a string
   */
  public static final String NS = "http://www.w3.org/ns/activitystreams#";

  /** returns the URI for this schema
      @return the URI for this schema
   */
  public static String getURI()
  { return NS; }

  protected static final Resource resource( String local )
  { return ResourceFactory.createResource( NS + local ); }

  protected static final Property property( String local )
  { return ResourceFactory.createProperty( NS, local ); }

  public final static Resource Object = resource( "Object" );
  public final static Resource Link = resource( "Link" );
  public final static Resource Activity = resource( "Activity" );
  public final static Resource IntransitiveActivity = resource( "IntransitiveActivity" );
  public final static Resource Collection = resource( "Collection" );
  public final static Resource OrderedCollection = resource( "OrderedCollection" );
  public final static Resource CollectionPage = resource( "CollectionPage" );
  public final static Resource OrderedCollectionPage = resource( "OrderedCollectionPage" );
  public final static Resource Accept = resource( "Accept" );
  public final static Resource TentativeAccept = resource( "TentativeAccept" );
  public final static Resource Add = resource( "Add" );
  public final static Resource Arrive = resource( "Arrive" );
  public final static Resource Create = resource( "Create" );
  public final static Resource Delete = resource( "Delete" );
  public final static Resource Follow = resource( "Follow" );
  public final static Resource Ignore = resource( "Ignore" );
  public final static Resource Join = resource( "Join" );
  public final static Resource Leave = resource( "Leave" );
  public final static Resource Like = resource( "Like" );
  public final static Resource Offer = resource( "Offer" );
  public final static Resource Invite = resource( "Invite" );
  public final static Resource Reject = resource( "Reject" );
  public final static Resource TentativeReject = resource( "TentativeReject" );
  public final static Resource Remove = resource( "Remove" );
  public final static Resource Undo = resource( "Undo" );
  public final static Resource Update = resource( "Update" );
  public final static Resource View = resource( "View" );
  public final static Resource Listen = resource( "Listen" );
  public final static Resource Read = resource( "Read" );
  public final static Resource Move = resource( "Move" );
  public final static Resource Travel = resource( "Travel" );
  public final static Resource Announce = resource( "Announce" );
  public final static Resource Block = resource( "Block" );
  public final static Resource Flag = resource( "Flag" );
  public final static Resource Dislike = resource( "Dislike" );
  public final static Resource Question = resource( "Question" );
  public final static Resource Application = resource( "Application" );
  public final static Resource Group = resource( "Group" );
  public final static Resource Organization = resource( "Organization" );
  public final static Resource Person = resource( "Person" );
  public final static Resource Service = resource( "Service" );
  public final static Resource Relationship = resource( "Relationship" );
  public final static Resource Article = resource( "Article" );
  public final static Resource Document = resource( "Document" );
  public final static Resource Audio = resource( "Audio" );
  public final static Resource Image = resource( "Image" );
  public final static Resource Video = resource( "Video" );
  public final static Resource Note = resource( "Note" );
  public final static Resource Page = resource( "Page" );
  public final static Resource Event = resource( "Event" );
  public final static Resource Place = resource( "Place" );
  public final static Resource Mention = resource( "Mention" );
  public final static Resource Profile = resource( "Profile" );
  public final static Resource Tombstone = resource( "Tombstone" );
  public final static Property actor = property( "actor" );
  public final static Property attachment = property( "attachment" );
  public final static Property attributedTo = property( "attributedTo" );
  public final static Property audience = property( "audience" );
  public final static Property bcc = property( "bcc" );
  public final static Property bto = property( "bto" );
  public final static Property cc = property( "cc" );
  public final static Property context = property( "context" );
  public final static Property current = property( "current" );
  public final static Property first = property( "first" );
  public final static Property generator = property( "generator" );
  public final static Property icon = property( "icon" );
  public final static Property image = property( "image" );
  public final static Property inReplyTo = property( "inReplyTo" );
  public final static Property instrument = property( "instrument" );
  public final static Property last = property( "last" );
  public final static Property location = property( "location" );
  public final static Property items = property( "items" );
  public final static Property oneOf = property( "oneOf" );
  public final static Property anyOf = property( "anyOf" );
  public final static Property closed = property( "closed" );
  public final static Property origin = property( "origin" );
  public final static Property next = property( "next" );
  public final static Property object = property( "object" );
  public final static Property prev = property( "prev" );
  public final static Property preview = property( "preview" );
  public final static Property result = property( "result" );
  public final static Property replies = property( "replies" );
  public final static Property tag = property( "tag" );
  public final static Property target = property( "target" );
  public final static Property to = property( "to" );
  public final static Property url = property( "url" );
  public final static Property accuracy = property( "accuracy" );
  public final static Property altitude = property( "altitude" );
  public final static Property content = property( "content" );
  public final static Property name = property( "name" );
  public final static Property duration = property( "duration" );
  public final static Property height = property( "height" );
  public final static Property href = property( "href" );
  public final static Property hreflang = property( "hreflang" );
  public final static Property partOf = property( "partOf" );
  public final static Property latitude = property( "latitude" );
  public final static Property longitude = property( "longitude" );
  public final static Property mediaType = property( "mediaType" );
  public final static Property endTime = property( "endTime" );
  public final static Property published = property( "published" );
  public final static Property startTime = property( "startTime" );
  public final static Property radius = property( "radius" );
  public final static Property rel = property( "rel" );
  public final static Property startIndex = property( "startIndex" );
  public final static Property summary = property( "summary" );
  public final static Property totalItems = property( "totalItems" );
  public final static Property units = property( "units" );
  public final static Property updated = property( "updated" );
  public final static Property width = property( "width" );
  public final static Property subject = property( "subject" );
  public final static Property relationship = property( "relationship" );
  public final static Property describes = property( "describes" );
  public final static Property formerType = property( "formerType" );
  public final static Property deleted = property( "deleted" );
}
