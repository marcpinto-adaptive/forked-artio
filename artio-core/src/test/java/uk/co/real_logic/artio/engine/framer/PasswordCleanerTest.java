/*
 * Copyright 2019 Adaptive Financial Consulting Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.artio.engine.framer;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class PasswordCleanerTest
{
    // TODO: checksums?
    // TODO: user request

    private static final String EXAMPLE_LOGON =
        "8=FIX.4.4\0019=099\00135=A\00149=initiator\00156=acceptor\00134=1\00152=20191002-16:54:47.446" +
        "\00198=0\001108=10\001141=N\001553=bob\001554=Uv1aegoh\00110=062\001";

    private static final String EXPECTED_CLEANED_LOGON =
        "8=FIX.4.4\0019=094\00135=A\00149=initiator\00156=acceptor\00134=1\00152=20191002-16:54:47.446" +
        "\00198=0\001108=10\001141=N\001553=bob\001554=***\00110=062\001";

    private static final String NO_PASSWORD_LOGON =
        "8=FIX.4.4\0019=78\00135=A\00149=initiator\00156=acceptor\00134=1\00152=20191002-16:54:47.446" +
        "\00198=0\001108=10\001141=N\00110=062\001";

    private final PasswordCleaner passwordCleaner = new PasswordCleaner();

    private final int offset;

    public PasswordCleanerTest(final int offset)
    {
        this.offset = offset;
    }

    @Test
    public void shouldCleanPasswordFromLogon()
    {
        shouldCleanMessage(EXAMPLE_LOGON, EXPECTED_CLEANED_LOGON);
    }

    @Test
    public void shouldNotChangeLogonWithoutPassword()
    {
        shouldCleanMessage(NO_PASSWORD_LOGON, NO_PASSWORD_LOGON);
    }

    private void shouldCleanMessage(final String inputMessage, final String expectedCleanedMessage)
    {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[1024]);
        final int length = buffer.putStringWithoutLengthAscii(offset, inputMessage);

        passwordCleaner.clean(buffer, offset, length);

        final DirectBuffer directBuffer = passwordCleaner.cleanedBuffer();
        final int cleanedLength = passwordCleaner.cleanedLength();
        final String cleanedLogon = directBuffer.getStringWithoutLengthAscii(0, cleanedLength);
        assertEquals(expectedCleanedMessage, cleanedLogon);
    }

    @Parameterized.Parameters(name = "offset={0}")
    public static Iterable<Object[]> decimalFloatCodecData()
    {
        return Arrays.asList(new Object[][]
            {
                {0},
                {100}
            });
    }
}