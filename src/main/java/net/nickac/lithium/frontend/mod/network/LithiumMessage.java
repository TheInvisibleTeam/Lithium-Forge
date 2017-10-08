/*
 * MIT License
 *
 * Copyright (c) 2017 NickAc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package net.nickac.lithium.frontend.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.nickac.lithium.backend.controls.LControl;
import net.nickac.lithium.backend.controls.impl.LWindow;
import net.nickac.lithium.backend.serializer.SerializationUtils;
import net.nickac.lithium.frontend.mod.LithiumMod;
import net.nickac.lithium.frontend.mod.ui.NewLithiumGUI;

import static net.nickac.lithium.backend.other.LithiumConstants.*;
import static net.nickac.lithium.frontend.mod.utils.NativeUtils.readUTF8String;

/**
 * Created by NickAc for Lithium!<br><br>
 * Original class made by diesieben07
 */
public class LithiumMessage implements IMessage {

	private String text;

	public LithiumMessage() {
	}

	public LithiumMessage(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		text = readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, text);
	}

	public static class Handle implements IMessageHandler<LithiumMessage, IMessage> {

		@Override
		public IMessage onMessage(LithiumMessage message, MessageContext ctx) {
			//TODO: Handle all other stuff
			String receivedMessage = message.text.trim();
			//System.out.println(String.format("Received %s.", message.text.trim()));
			if (receivedMessage.startsWith(LITHIUM_RECEIVE_WINDOW)) {
				String w = receivedMessage.substring(LITHIUM_RECEIVE_WINDOW.length());

				LWindow receivedWindow = SerializationUtils.stringToObject(w, LWindow.class);

				if (receivedWindow != null) {
					LithiumMod.getSimpleNetworkWrapper().sendToServer(new LithiumMessage(LITHIUM_WINDOW_OPEN + receivedWindow.getUUID()));
					Minecraft.getMinecraft().addScheduledTask(() -> {
						NewLithiumGUI gui = new NewLithiumGUI(receivedWindow);
						Minecraft.getMinecraft().displayGuiScreen(gui);
					});

				}
			} else if (receivedMessage.startsWith(LITHIUM_CONTROL_CHANGED)) {
				String c = receivedMessage.substring(LITHIUM_CONTROL_CHANGED.length());
				LControl newC = SerializationUtils.stringToObject(c, LControl.class);
				if (LithiumMod.getCurrentLithium() != null && newC != null) {
					LithiumMod.replaceControl(LithiumMod.getCurrentLithium().getBaseWindow(), newC.getUUID(), newC);
				}

			} else if (receivedMessage.equals(LITHIUM_CLOSE_WINDOW)) {
				Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(null));
			}
			//System.out.println(String.format("Received %s.", message.text.trim()));
			return null;
		}
	}

}