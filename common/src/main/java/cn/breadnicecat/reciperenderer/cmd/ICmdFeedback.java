package cn.breadnicecat.reciperenderer.cmd;

import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

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
	
	static ICmdFeedback create(Consumer<Component> feedback, Consumer<Component> error) {
		return new CmdFeedbackImpl(feedback, error);
	}
	
	class CmdFeedbackImpl implements ICmdFeedback {
		final Consumer<Component> feedback;
		final Consumer<Component> error;
		
		CmdFeedbackImpl(Consumer<Component> feedback, Consumer<Component> error) {
			this.feedback = feedback;
			this.error = error;
		}
		
		@Override
		public void sendFeedback(Component message) {
			feedback.accept(message);
		}
		
		@Override
		public void sendError(Component message) {
			error.accept(message);
		}
	}
}
