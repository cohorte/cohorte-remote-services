/**
 * Copyright 2013 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cohorte.remote.multicast.utils;

import java.net.InetSocketAddress;

/**
 * Defines a UDP packets listener
 * 
 * @author Thomas Calmant
 */
public interface IPacketListener {

    /**
     * Handles an exception thrown while waiting or a packet
     * 
     * @param aException
     *            The thrown exception
     * @return True to continue the thread loop, else false
     */
    boolean handleError(Exception aException);

    /**
     * Handles a received UDP packet
     * 
     * @param aSender
     *            Address of the packet sender
     * @param aContent
     *            Content of the packet
     */
    void handlePacket(InetSocketAddress aSender, byte[] aContent);
}
