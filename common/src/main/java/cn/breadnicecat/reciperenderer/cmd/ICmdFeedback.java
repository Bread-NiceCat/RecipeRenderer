package cn.breadnicecat.reciperenderer.cmd;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.mojang.text2speech.Narrator.LOGGER;

/**
 * Created in 2024/7/9 上午2:37
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public interface ICmdFeedback {
	/**
	 * Sends a feedback message to the player.
	 *
	 * @param message the feedback message
	 */
	void sendFeedback(Component message);
	
	/**
	 * Sends an error message to the player.
	 *
	 * @param message the error message
	 */
	void sendError(Component message);
	
	default boolean isPlayer() {
		return getEntity() instanceof Player;
	}
	
	Entity getEntity();
	
	static ICmdFeedback create(Supplier<Entity> entity, Consumer<Component> feedback, Consumer<Component> error) {
		return new CmdFeedbackImpl(entity, feedback, error);
	}
	
	class CmdFeedbackImpl implements ICmdFeedback {
		final Supplier<Entity> entity;
		final Consumer<Component> feedback;
		final Consumer<Component> error;
		
		CmdFeedbackImpl(Supplier<Entity> entity, Consumer<Component> feedback, Consumer<Component> error) {
			this.entity = entity;
			this.feedback = feedback;
			this.error = error;
		}
		
		@Override
		public void sendFeedback(Component message) {
			LOGGER.info(message.getString());
			feedback.accept(message);
		}
		
		@Override
		public void sendError(Component message) {
			LOGGER.error(message.getString());
			error.accept(message);
		}
		
		
		@Override
		public Entity getEntity() {
			return entity.get();
		}
	}
}
