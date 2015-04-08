/*
 * Copyright 2015 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.fix_gateway.session;

import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;
import uk.co.real_logic.fix_gateway.builder.*;
import uk.co.real_logic.fix_gateway.decoder.*;
import uk.co.real_logic.fix_gateway.replication.GatewayPublication;
import uk.co.real_logic.fix_gateway.util.MutableAsciiFlyweight;

/**
 * Encapsulates sending messages relating to sessions
 */
public class SessionProxy
{

    private final LogonEncoder logon = new LogonEncoder();
    private final ResendRequestEncoder resendRequest = new ResendRequestEncoder();
    private final LogoutEncoder logout = new LogoutEncoder();
    private final HeartbeatEncoder heartbeat = new HeartbeatEncoder();
    private final RejectEncoder reject = new RejectEncoder();

    private final UnsafeBuffer buffer;
    private final MutableAsciiFlyweight string;
    private final GatewayPublication gatewayPublication;
    private final SessionIdStrategy sessionIdStrategy;
    private final SessionIds senderSessions;

    public SessionProxy(
        final int bufferSize,
        final GatewayPublication gatewayPublication,
        final SessionIdStrategy sessionIdStrategy,
        final SessionIds senderSessions)
    {
        this.gatewayPublication = gatewayPublication;
        this.sessionIdStrategy = sessionIdStrategy;
        this.senderSessions = senderSessions;
        buffer = new UnsafeBuffer(new byte[bufferSize]);
        string = new MutableAsciiFlyweight(buffer);
    }

    public void resendRequest(final int msgSeqNo, final int beginSeqNo, final int endSeqNo, final long sessionId)
    {
        final HeaderEncoder header = resendRequest.header();
        sessionIdStrategy.onSend(senderSessions.get(sessionId), header);
        header.msgSeqNum(msgSeqNo);
        resendRequest.beginSeqNo(beginSeqNo)
                     .endSeqNo(endSeqNo);
        send(resendRequest.encode(string, 0), sessionId, ResendRequestDecoder.MESSAGE_TYPE);
    }

    /**
     * NB: Refers to a connectionId because the session may disconnect before a session id is associated
     * with it.
     *
     * @param connectionId
     */
    public void disconnect(final long connectionId)
    {
        gatewayPublication.saveDisconnect(connectionId);
    }

    public void logon(final int heartbeatInterval, final int msgSeqNo, final long sessionId)
    {
        final HeaderEncoder header = logon.header();
        setupHeader(header, sessionId);
        header.msgSeqNum(msgSeqNo);

        logon.heartBtInt(heartbeatInterval);
        send(logon.encode(string, 0), sessionId, LogonDecoder.MESSAGE_TYPE);
    }

    public void logout(final int msgSeqNo, final long sessionId)
    {
        final HeaderEncoder header = logout.header();
        setupHeader(header, sessionId);
        header.msgSeqNum(msgSeqNo);

        send(logout.encode(string, 0), sessionId, LogoutDecoder.MESSAGE_TYPE);
    }

    public void heartbeat(final String testReqId, final long sessionId)
    {
        final HeaderEncoder header = heartbeat.header();
        setupHeader(header, sessionId);
        // TODO: header.msgSeqNum(0);

        if (testReqId != null)
        {
            heartbeat.testReqID(testReqId);
        }
        send(heartbeat.encode(string, 0), sessionId, HeartbeatDecoder.MESSAGE_TYPE);
    }

    public void reject(final int msgSeqNo, final int refSeqNum, final long sessionId)
    {
        final HeaderEncoder header = reject.header();
        setupHeader(header, sessionId);
        header.msgSeqNum(msgSeqNo);

        reject.refSeqNum(refSeqNum);
        // TODO: decide on other ref fields
        send(reject.encode(string, 0), sessionId, RejectDecoder.MESSAGE_TYPE);
    }

    private void setupHeader(final HeaderEncoder header, final long sessionId)
    {
        sessionIdStrategy.onSend(senderSessions.get(sessionId), header);
        header.sendingTime(System.currentTimeMillis());
    }

    private void send(final int length, final long sessionId, final int messageType)
    {
        gatewayPublication.saveMessage(buffer, 0, length, sessionId, messageType);
    }
}
