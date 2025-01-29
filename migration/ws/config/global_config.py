from os.path import dirname
from pathlib import Path
from attrdict import AttrDict

__all__ = ['config']

config = None


def init():
    global config
    if config is None:
        config = AttrDict()
        config.BASE_PATH = dirname(dirname(__file__))
        config.BASE_PPATH = Path(dirname(dirname(__file__)))


if __name__ == "__main__":
    init()
