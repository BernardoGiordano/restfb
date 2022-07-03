/*
 * Copyright (c) 2010-2022 Mark Allen, Norbert Bartels.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.restfb.types;

import com.restfb.AbstractJsonMapperTests;
import com.restfb.types.webhook.ChangeValue;
import com.restfb.types.webhook.WebhookEntry;
import com.restfb.types.webhook.WebhookObject;
import com.restfb.types.webhook.whatsapp.WhatsappMessagesValue;
import com.restfb.types.whatsapp.platform.Contact;
import com.restfb.types.whatsapp.platform.Message;
import com.restfb.types.whatsapp.platform.message.Image;
import com.restfb.types.whatsapp.platform.message.Video;
import com.restfb.types.whatsapp.platform.message.MessageType;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class WABPwebhookTest extends AbstractJsonMapperTests {

  @Test
  void incomingMessageText() {
    WhatsappMessagesValue change = getWHObjectFromJson("webhook-incoming-message-text", WhatsappMessagesValue.class);
    assertThat(change).isInstanceOf(WhatsappMessagesValue.class);

    // check contact
    checkContact(change);

    // check Metadata
    checkMetaData(change);

    // check Message
    assertThat(change.getMessages()).hasSize(1);
    com.restfb.types.whatsapp.platform.Message message = change.getMessages().get(0);
    assertThat(message.getText()).isNotNull();
    assertThat(message.getText().getBody()).isEqualTo("Test");
    assertThat(message.getTimestamp()).isEqualTo(new Date(1653253313000L));
    assertThat(message.getType()).isEqualTo(MessageType.text);
    assertThat(message.getFrom()).isEqualTo("491234567890");
    assertThat(message.isText()).isTrue();
  }

  @Test
  void incomingMessageImage() {
    WhatsappMessagesValue change = getWHObjectFromJson("webhook-incoming-message-image", WhatsappMessagesValue.class);
    assertThat(change).isInstanceOf(WhatsappMessagesValue.class);

    // check contact
    checkContact(change);

    // check Metadata
    checkMetaData(change);

    // check Message
    assertThat(change.getMessages()).hasSize(1);
    Message message = change.getMessages().get(0);
    assertThat(message.getImage()).isNotNull();
    assertThat(message.isImage()).isTrue();
    assertThat(message.isText()).isFalse();

    Image image = message.getImage();
    assertThat(image.getCaption()).isEqualTo("Some awesome image");
    assertThat(image.getId()).isEqualTo("400962571939895");
    assertThat(image.getMimeType()).isEqualTo("image\\/jpeg");
    assertThat(image.getSha256()).isEqualTo("law0CgE277wMMnIJU0XIxhDne6Ptmwaek\\/thVM7mVtg=");

    assertThat(message.getTimestamp()).isEqualTo(new Date(1653253313000L));
    assertThat(message.getType()).isEqualTo(MessageType.image);
    assertThat(message.getFrom()).isEqualTo("491234567890");
  }

  @Test
  void incomingMessageVideo() {
    WhatsappMessagesValue change = getWHObjectFromJson("webhook-incoming-message-video", WhatsappMessagesValue.class);
    assertThat(change).isInstanceOf(WhatsappMessagesValue.class);

    checkContact(change);

    checkMetaData(change);

    // check Message
    assertThat(change.getMessages()).hasSize(1);
    Message message = change.getMessages().get(0);
    assertThat(message.getVideo()).isNotNull();
    assertThat(message.isImage()).isFalse();
    assertThat(message.isText()).isFalse();
    assertThat(message.isVideo()).isTrue();

    Video image = message.getVideo();
    assertThat(image.getCaption()).isEqualTo("Video");
    assertThat(image.getId()).isEqualTo("378911494190301");
    assertThat(image.getMimeType()).isEqualTo("video\\/mp4");
    assertThat(image.getSha256()).isEqualTo("r9TvFx8eGPq\\/nn7l\\/yVMfSmQjBPWlEGOpkBKMVNqDGk=");

    assertThat(message.getTimestamp()).isEqualTo(new Date(1653253313000L));
    assertThat(message.getType()).isEqualTo(MessageType.video);
    assertThat(message.getFrom()).isEqualTo("491234567890");
  }

  private void checkMetaData(WhatsappMessagesValue change) {
    // check Metadata
    assertThat(change.getMetadata()).isNotNull();
    assertThat(change.getMetadata().getDisplayPhoneNumber()).isEqualTo("1234567891");
    assertThat(change.getMetadata().getPhoneNumberId()).isEqualTo("10634295353625");
  }

  private void checkContact(WhatsappMessagesValue change) {
    // check contact
    assertThat(change.getContacts()).hasSize(1);
    Contact contact = change.getContacts().get(0);
    assertThat(contact.getWaId()).isEqualTo("491234567890");
    assertThat(contact.getProfile()).isNotNull();
    assertThat(contact.getProfile().getName()).isEqualTo("TestUser");
  }

  private <T extends ChangeValue> T getWHObjectFromJson(String jsonName, Class<T> clazz) {
    WebhookObject webhookObject =
        createJsonMapper().toJavaObject(jsonFromClasspath("whatsapp/" + jsonName), WebhookObject.class);
    assertThat(webhookObject.isWhatsAppBusinessAccount()).isTrue();
    assertThat(webhookObject.getEntryList()).hasSize(1);
    WebhookEntry entry = webhookObject.getEntryList().get(0);
    return entry.getChanges().get(0).getValue().convertChangeValue(clazz);
  }
}
