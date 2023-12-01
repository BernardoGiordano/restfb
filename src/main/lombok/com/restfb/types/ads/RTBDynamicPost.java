/*
 * Copyright (c) 2010-2024 Mark Allen, Norbert Bartels.
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
package com.restfb.types.ads;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.restfb.Facebook;
import com.restfb.types.Comments;
import com.restfb.types.FacebookType;
import com.restfb.types.Likes;

import lombok.Getter;
import lombok.Setter;

public class RTBDynamicPost extends FacebookType {

  @Getter
  @Setter
  @Facebook("child_attachments")
  private List<DynamicPostChildAttachment> childAttachments = new ArrayList<>();

  @Getter
  @Setter
  @Facebook
  private Date created;

  @Getter
  @Setter
  @Facebook
  private String description;

  @Getter
  @Setter
  @Facebook("image_url")
  private String imageUrl;

  @Getter
  @Setter
  @Facebook
  private String link;

  @Getter
  @Setter
  @Facebook
  private String message;

  @Getter
  @Setter
  @Facebook("owner_id")
  private String ownerId;

  @Getter
  @Setter
  @Facebook("place_id")
  private String placeId;

  @Getter
  @Setter
  @Facebook("product_id")
  private String productId;

  @Getter
  @Setter
  @Facebook
  private String title;

  @Getter
  @Setter
  @Facebook
  private Comments comments;

  @Getter
  @Setter
  @Facebook
  private Likes likes;

}
