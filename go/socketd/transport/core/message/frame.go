package message

import (
	"fmt"
)

type Frame struct {
	Len     uint32
	Flag    uint32
	Message *Message
}

func NewFrame(flag uint32, msg *Message) *Frame {
	return &Frame{
		Len:     0,
		Flag:    0,
		Message: msg,
	}
}

func (f *Frame) String() string {
	if f.Message != nil {
		return fmt.Sprintf("%+v", f.Message)
	}
	return fmt.Sprintf("{Len: %d, Flag: %d}", f.Len, f.Flag)
}