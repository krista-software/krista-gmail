/*
 * Gmail Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.krista.extensions.essentials.collaboration.gmail.catalog.entities;

import app.krista.extension.impl.anno.*;
import app.krista.model.base.File;

import java.util.List;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "b94af183-4891-4b54-a9b0-d6096b361fc7")
@Entity(name = "Mail Details", id = "localDomainEntity_0fb99723-377c-419e-b24e-0ab0ce948e8c", primaryKey = "Message ID", supportStore = false)
public class MailDetails {

    @Field(name = "From", type = "Text", required = false)
    public String from;

    @Field(name = "To", type = "Text")
    public String to;

    @Field(name = "Message", type = "RichText")
    public String message;

    @Searchable
    @Field(name = "Subject", type = "Text")
    public String subject;

    @Field(name = "File Attachment", type = "File", required = false)
    public List<File> fileAttachment;

    @Field.Desc(name = "Item attachment", type = "[ Label ]", required = false)
    public List<String> itemAttachment;

    @Field(name = "Reference attachment", type = "File", required = false)
    public List<File> referenceAttachment;

    @Field(name = "Message ID", type = "Text", required = false)
    public String messageID;

    @Field(name = "Cc", type = "Text", required = false)
    public String cc;

    @Field(name = "Bcc", type = "Text", required = false)
    public String bcc;

    @Field(name = "Is Read", type = "Switch", required = false)
    public Boolean isRead;

    @Field(name = "ReplyTo", type = "Text", required = false)
    public String replyTo;

    @Field(name = "Send Date and Time", type = "Date", required = false, attributes = @Attribute(name = "includeTimeOfDay", value = "true"))
    public Long sendDateAndTime;

    @Field(name = "Received Date and Time", type = "Date", required = false, attributes = @Attribute(name = "includeTimeOfDay", value = "true"))
    public Long receivedDateAndTime;
}