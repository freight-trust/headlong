/*
   Copyright 2019 Evan Saulpaugh

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.esaulpaugh.headlong.rlp;

import com.esaulpaugh.headlong.rlp.exception.DecodeException;
import com.esaulpaugh.headlong.rlp.util.Integers;

import java.util.*;

import static com.esaulpaugh.headlong.rlp.DataType.LIST_LONG_OFFSET;

/**
 * Created by Evo on 1/19/2017.
 */
public final class RLPList extends RLPItem implements Iterable<RLPItem> {

    RLPList(byte lead, DataType type, byte[] buffer, int index, int containerEnd, boolean lenient) throws DecodeException {
        super(lead, type, buffer, index, containerEnd, lenient);
    }

    @Override
    public boolean isList() {
        return true;
    }

    /**
     * @param srcElements pre-encoded top-level elements of the list
     */
    static RLPList withElements(Iterable<RLPItem> srcElements) {
        int dataLen = 0;
        for (RLPItem e : srcElements) {
            dataLen += e.encodingLength();
        }

        byte[] dest;
        if (dataLen < DataType.MIN_LONG_DATA_LEN) {
            dest = new byte[1 + dataLen];
            dest[0] = (byte) (DataType.LIST_SHORT_OFFSET + dataLen);
            copyElements(srcElements, dest, 1);
        } else {
            dest = encodeListLong(dataLen, srcElements);
        }
        try {
            byte lead = dest[0];
            return new RLPList(lead, DataType.type(lead), dest, 0, dest.length, false);
        } catch (DecodeException de) {
            throw new RuntimeException(de);
        }
    }

    private static void copyElements(Iterable<RLPItem> srcElements, byte[] dest, int destIndex) {
        for (final RLPItem element : srcElements) {
            final int elementLen = element.encodingLength();
            System.arraycopy(element.buffer, element.index, dest, destIndex, elementLen);
            destIndex += elementLen;
        }
    }

    private static byte[] encodeListLong(final int srcDataLen, final Iterable<RLPItem> srcElements) {
        byte[] length = Integers.toBytes(srcDataLen);
        int destHeaderLen = 1 + length.length;
        byte[] dest = new byte[destHeaderLen + srcDataLen];
        dest[0] = (byte) (LIST_LONG_OFFSET + length.length);
        System.arraycopy(length, 0, dest, 1, length.length);
        copyElements(srcElements, dest, destHeaderLen);
        return dest;
    }

    public List<RLPItem> elements(RLPDecoder decoder) throws DecodeException {
        ArrayList<RLPItem> arrayList = new ArrayList<>();
        elements(decoder, arrayList);
        return arrayList;
    }

    public void elements(RLPDecoder decoder, Collection<RLPItem> collection) throws DecodeException {
        int i = dataIndex;
        while (i < this.endIndex) {
            RLPItem item = decoder.wrap(buffer, i, this.endIndex);
            collection.add(item);
            i = item.endIndex;
        }
    }
    
    @Override
    public RLPList duplicate(RLPDecoder decoder) throws DecodeException {
        return decoder.wrapList(encoding(), 0);
    }

    public RLPListIterator iterator(RLPDecoder decoder) {
        return new RLPListIterator(this, decoder);
    }

    @Override
    public Iterator<RLPItem> iterator() {
        return new Iterator<RLPItem>() {

            private final RLPListIterator iter = iterator(RLPDecoder.RLP_STRICT);

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public RLPItem next() {
                try {
                    return iter.next();
                } catch (DecodeException e) {
                    throw new NoSuchElementException(e.getMessage()); // *** beware of RuntimeException ***
                }
            }
        };
    }
}
