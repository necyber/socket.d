import asyncio
import typing
from concurrent.futures import Executor
from threading import Thread


class AsyncUtil(object):

    @staticmethod
    def thread_handler(_loop: asyncio.AbstractEventLoop, fn: asyncio.Task):
        """
        静态方法 thread_handler 用于在指定的事件循环中运行一个协程函数。

        参数:
            _loop (EventLoop): 事件循环对象，用于控制协程的执行。
            fn (Coroutine): 需要运行的协程函数。
        """
        # asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())
        asyncio.set_event_loop(_loop)
        return _loop.run_until_complete(fn)

    @staticmethod
    def run_forever(loop: asyncio.AbstractEventLoop):
        future = None

        def _main(_loop: asyncio.AbstractEventLoop):
            nonlocal future
            asyncio.set_event_loop(_loop)
            future = asyncio.Future()

            async def _run():
                await future

            _loop.run_until_complete(_run())

        t = Thread(target=_main, args=(loop,))
        t.start()
        return future

    @staticmethod
    def thread_loop(core: typing.Coroutine, thread=None, pool: Executor = None):
        loop = asyncio.new_event_loop()
        if thread:
            t = Thread(target=lambda: AsyncUtil.thread_handler(loop, loop.create_task(core)))
            t.start()
        if pool:
            pool.submit(lambda: loop.run_until_complete(core))
