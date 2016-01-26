/*
 * SonarLint Core Library
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.sonarlint.core.util;

import org.junit.Test;
import org.sonarsource.sonarlint.core.util.BatchUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchUtilsTest {

  @Test
  public void encodeForUrl() throws Exception {
    assertThat(BatchUtils.encodeForUrl(null)).isEqualTo("");
    assertThat(BatchUtils.encodeForUrl("")).isEqualTo("");
    assertThat(BatchUtils.encodeForUrl("foo")).isEqualTo("foo");
    assertThat(BatchUtils.encodeForUrl("foo&bar")).isEqualTo("foo%26bar");
  }

  @Test
  
  public void testDescribe() {
    Object withToString = new Object() {
      @Override
      public String toString() {
        return "desc";
      }
    };

    Object withoutToString = new Object();

    assertThat(BatchUtils.describe(withToString)).isEqualTo(("desc"));
    assertThat(BatchUtils.describe(withoutToString)).isEqualTo("java.lang.Object");
  }
}