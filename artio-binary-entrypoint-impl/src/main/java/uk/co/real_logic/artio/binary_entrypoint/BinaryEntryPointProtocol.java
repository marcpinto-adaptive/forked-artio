/*
 * Copyright 2021 Monotonic Ltd.
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
package uk.co.real_logic.artio.binary_entrypoint;

import io.aeron.ExclusivePublication;
import org.agrona.concurrent.EpochNanoClock;
import uk.co.real_logic.artio.fixp.BinaryFixPProtocol;
import uk.co.real_logic.artio.library.ILink3Connection;

public class BinaryEntryPointProtocol extends BinaryFixPProtocol
{
    public BinaryEntryPointParser makeParser(final ILink3Connection session)
    {
        return new BinaryEntryPointParser();
    }

    public BinaryEntryPointProxy makeProxy(
        final ExclusivePublication publication, final EpochNanoClock epochNanoClock)
    {
        return new BinaryEntryPointProxy();
    }

    public BinaryEntryPointOffsets makeOffsets()
    {
        return new BinaryEntryPointOffsets();
    }
}