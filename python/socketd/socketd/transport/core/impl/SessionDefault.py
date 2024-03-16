
from abc import ABC
from typing import Optional

from socketd.transport.core.entity.MessageBuilder import MessageBuilder
from socketd.transport.core.impl.SessionBase import SessionBase
from socketd.transport.core.Channel import Channel
from socketd.transport.core.HandshakeDefault import HandshakeDefault
from socketd.transport.core.Entity import Entity
from socketd.transport.core.Message import Message
from socketd.transport.core.Frame import Frame
from socketd.transport.core.Costants import Constants
from socketd.transport.core.Flags import Flags
from socketd.transport.stream.RequestStream import RequestStream
from socketd.transport.stream.SendStream import SendStream

from socketd.transport.stream.SubscribeStream import SubscribeStream

from loguru import logger


class SessionDefault(SessionBase, ABC):

    def __init__(self, channel: Channel):
        super().__init__(channel)
        self.__path_new = None

    def is_valid(self) -> bool:
        return self._channel.is_valid()

    def is_closing(self) ->bool:
        return self._channel.is_closing()

    def get_remote_address(self) -> str:
        return self._channel.get_remote_address()

    def get_local_address(self) -> str:
        return self._channel.get_local_address()

    def get_handshake(self) -> HandshakeDefault:
        return self._channel.get_handshake()

    async def send_ping(self):
        await self._channel.send_ping()

    async def send(self, topic: str, content: Entity) -> SendStream:
        message = MessageBuilder().sid(self.generate_id()).event(topic).entity(content).build()

        stream: SendStream = SendStream(message.sid())
        await self._channel.send(Frame(Flags.Message, message), stream)
        return stream

    async def send_and_request(self, event: str, content: Entity,
                               timeout: int = 100) -> RequestStream:

        if timeout < 100:
            timeout = self._channel.get_config().get_request_timeout() / 1000
        message = MessageBuilder().sid(self.generate_id()).event(event).entity(content).build()
        stream = RequestStream(message.sid(), timeout)
        await self._channel.send(Frame(Flags.Request, message), stream)
        return stream

    async def send_and_subscribe(self, event: str, content: Entity, timeout: int = 0):
        message = MessageBuilder().sid(self.generate_id()).event(event).entity(content).build()
        stream = SubscribeStream(message.sid(), timeout)
        await self._channel.send(Frame(Flags.Subscribe, message), stream)
        return stream

    async def reply(self, from_msg: Message, content: Entity):
        message = MessageBuilder().sid(from_msg.sid()).event(from_msg.event()).entity(content).build()

        await self._channel.send(Frame(Flags.Reply, message), None)

    async def reply_end(self, from_msg: Message, content: Entity):
        message = MessageBuilder().sid(from_msg.sid()).event(from_msg.event()).entity(content).build()

        await self._channel.send(Frame(Flags.ReplyEnd, message), None)

    async def reconnect(self):
        await self._channel.reconnect()

    async def close(self):
        if self._channel.is_valid():
            try:
                await self._channel.send_close()
            except Exception as e:
                logger.warning(f" {self._channel.get_config().get_role_name()} channel send_close error {e}")
        await self._channel.close(Constants.CLOSE2009_USER)

    def get_param(self, name: str):
        return self.get_handshake().param(name)

    def pathNew(self, path: str):
        self.__path_new = path

    def path(self) -> Optional[str]:
        if path_new := self.__path_new:
            return path_new
        return self.get_handshake().uri().__str__()