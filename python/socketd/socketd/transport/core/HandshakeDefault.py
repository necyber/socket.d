from typing import Dict
from urllib.parse import urlparse, parse_qsl

from socketd.transport.core.EntityMetas import EntityMetas
from socketd.transport.core.Message import Message, MessageInternal


class Handshake:

    def get_uri(self): ...

    def get_param_map(self) -> Dict[str, str]: ...

    def get_param(self, name: str): ...

    def get_param_or_default(self, name, value): ...

    def get_version(self): ...

    def put_param(self, name, value): ...


class HandshakeInternal(Handshake):

    def get_source(self) -> MessageInternal: ...


class HandshakeDefault(HandshakeInternal):
    def __init__(self, message: MessageInternal):
        self.__source: MessageInternal = message
        linkUrl = message.data_as_string()
        if not linkUrl:
            linkUrl = message.event()
        self.__uri = urlparse(linkUrl)
        self.__path = self.__uri.path
        self.__entity = message.entity()
        self.__version = self.__entity.meta(EntityMetas.META_SOCKETD_VERSION)
        self.__param_map = self._parse_query_string(self.__uri.query)

    def get_uri(self):
        return self.__uri

    def get_param_map(self) -> Dict[str, str]:
        return self.__param_map

    def get_param(self, name: str):
        return self.__param_map.get(name)

    def get_version(self):
        return self.__version

    @staticmethod
    def _parse_query_string(query_string):
        params = {}
        if query_string:
            for name, value in parse_qsl(query_string):
                params[name] = value
        return params

    def get_source(self) -> Message:
        return self.__source

    def get_param_or_default(self, name, value):
        if data := self.__param_map.get(name):
            return data
        return value

    def put_param(self, name, value):
        self.__param_map[name] = value
