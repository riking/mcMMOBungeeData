/**
 * This package has classes that wrap the byte arrays of PluginMessages. The
 * methods in {@link Message} are implemented by
 * {@link AbstractProxyServerMessage} and {@link AbstractServerServerMessage},
 * forming two basic types of messages.
 * <p>
 * The Proxy-Server messages have no need for any indication of sender and
 * reciever: the other party is always the proxy for servers, and one of the
 * servers for the proxy. On the other hand, Server-Server messages need to
 * remember where they are going and where they came from, because they must
 * be piped through the BungeeCord.Forward channel.
 * <p>
 * The usage pattern goes like this:
 *
 * <h3>Sending</h3>
 * The plugin constructs one of the end classes, such as
 * VersionAnnounceMessage, using the "sender constructor" if appropriate, and
 * calls write() on a new ByteArrayOutputStream, which is used to send the
 * message.
 * <p>
 * <code>
 * ByteArrayOutputStream out = new ByteArrayOutputStream();
 * Message m = new VersionAnnounceMessage(); // sender constructor
 * m.write(out);
 * sendPluginMessage(any_player, out.toByteArray());
 * </code>
 *
 * <h3>Receiving</h3>
 * The plugin gets the byte[] from the PluginMessage, and calls
 * {@link PluginMessageUtil#readIncomingMessage(byte[])}. It decides what to
 * do based on the type of the returned Message.
 * <p>
 * <code>
 * Message m = PluginMessageUtil.readIncomingMessage(pm.getBytes());
 * if (m instanceof VersionAnnounceMessage) {
 *     // ...
 * } else if (m instanceof TransferPullMessage) {
 *     // ...
 * } else if // ...
 * } else {
 *     throw new AssertionError("Forgot to handle message type?");
 * }
 * </code>
 */
package me.riking.bungeemmo.common.messaging;