package aethereal.ui.widget;

import static aethereal.core.Interface.aM_;
import aethereal.core.Delta;
import aethereal.render.EasingList;
import aethereal.render.Fonts;

import aethereal.config.ThemeInfo;
import aethereal.core.GlobalEvent;
import aethereal.core.Interface;
import aethereal.event.BackendEvent;
import aethereal.event.DrawEvent;
import aethereal.event.PacketEvent;
import aethereal.notification.Notification;
import aethereal.ui.element.DragInfo;
import aethereal.ui.widget.Widget;

import aethereal.setting.BooleanSetting;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;

public class NotificationWidget extends Widget implements Interface {
    private final BooleanSetting f;
    private final BooleanSetting g;
    private boolean initialized = false;

    public NotificationWidget() {
        super(new DragInfo("Уведомления", 0.0f, 0.0f, 0.0f, 0.0f));
        this.f = new BooleanSetting("Оповещать о поднятии донат-предметов", true);
        this.g = new BooleanSetting("Обновления и уведомления друзей", true);
        j().a(this);
        j().a(0);
        a(this.g, this.f);
    }

    @Override
    public void a(GlobalEvent event) {
        d().a((aM_.currentScreen instanceof ChatScreen) || !Delta.h().d().m().b().isEmpty());
        super.a(event);
    }

    @Override
    public void a(DrawEvent event) {
        float fA;
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());

        if (!this.initialized && j().c() == 0.0f && j().d() == 0.0f) {
            float initX = (aM_.getWindow().getScaledWidth() - 150.0f) / 2.0f;
            float initY = 25.0f;
            j().a(initX);
            j().b(initY);
            this.initialized = true;
        }

        float x = j().a();
        float contentY = j().b();

        boolean hasNotifications = !Delta.h().d().m().b().isEmpty();
        if (!hasNotifications && (aM_.currentScreen instanceof ChatScreen)) {
            String previewText = "Пример отображения уведомления";
            float previewW = 17.5f + Fonts.e.a(previewText, this.e) + 6.0f;
            int color = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
            a(event, x, contentY, "o", previewText, previewW, a(), color);
            j().c(previewW);
            j().d(this.d);
            super.a(event);
            return;
        }

        float maxW = 100.0f;
        for (Notification notification : Delta.h().d().m().b()) {
            float animation = notification.a().c() * a();
            if (animation > 0.0f) {
                Object message = notification.c();
                if (message instanceof Text) {
                    Text value = (Text) message;
                    fA = Fonts.e.a(value, this.e);
                } else {
                    fA = Fonts.e.a(String.valueOf(message), this.e);
                }
                float width = 17.5f + fA + 4.0f;
                maxW = Math.max(maxW, width);
                int color = notification.e() == -1 ? Delta.h().d().o().a(ThemeInfo.PRIMARY).a() : notification.e();
                Object objD = notification.d();
                if (objD instanceof ItemStack) {
                    ItemStack stack = (ItemStack) objD;
                    a(event, x, contentY, stack, message, width, animation, color);
                } else {
                    a(event, x, contentY, (String) notification.d(), message, width, animation, color);
                }
                contentY += (this.d + 3.5f) * animation;
            }
        }
        j().c(maxW);
        j().d(Math.max(this.d, contentY - j().b()));
        super.a(event);
    }

    @Override
    public void a(PacketEvent event) {
        if (this.f.c().booleanValue() && event.c()) {
            ItemPickupAnimationS2CPacket class_2775VarD = (ItemPickupAnimationS2CPacket) event.d();
            if (class_2775VarD instanceof ItemPickupAnimationS2CPacket) {
                ItemPickupAnimationS2CPacket itemPickupAnimationS2CPacket = class_2775VarD;
                ClientPlayerEntity class_746VarMethod_8469 = (ClientPlayerEntity) aM_.world.getEntityById(itemPickupAnimationS2CPacket.getCollectorEntityId());
                if (class_746VarMethod_8469 instanceof PlayerEntity) {
                    ClientPlayerEntity class_746Var = (ClientPlayerEntity) (PlayerEntity) class_746VarMethod_8469;
                    ItemEntity class_1542VarMethod_8469 = (ItemEntity) aM_.world.getEntityById(itemPickupAnimationS2CPacket.getEntityId());
                    if (class_1542VarMethod_8469 instanceof ItemEntity) {
                        ItemEntity itemEntity = class_1542VarMethod_8469;
                        if (class_746Var != aM_.player && !itemEntity.getStack().getName().getString().contains("Упс.") && ((itemEntity.getStack().contains(DataComponentTypes.CUSTOM_NAME) && itemEntity.getStack().contains(DataComponentTypes.LORE)) || itemEntity.getStack().isOf(Items.ENCHANTED_GOLDEN_APPLE))) {
                            Delta.h().d().m().a(new Notification(itemEntity.getStack().copy(), class_746Var.getName().copy().append(" подобрал ").append(itemEntity.getStack().getName()).append(itemPickupAnimationS2CPacket.getStackAmount() > 1 ? " x" + itemPickupAnimationS2CPacket.getStackAmount() : ""), 1500));
                        }
                    }
                }
            }
        }
        super.a(event);
    }

    @Override
    public void a(BackendEvent event) {
        String message = event.d().a().a(event.d().c(), "message");
        if ("friend".equals(event.d().b()) && this.g.c().booleanValue() && message != null) {
            Delta.h().d().m().a(new Notification("o", message, 5000));
        }
        if ("application".equals(event.d().b()) && message != null) {
            Delta.h().d().m().a(new Notification("o", message, 15000));
        }
        super.a(event);
    }
}
