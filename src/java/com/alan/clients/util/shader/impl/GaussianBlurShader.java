package com.alan.clients.util.shader.impl;

import com.alan.clients.util.shader.base.RiseShader;
import com.alan.clients.util.shader.base.RiseShaderProgram;
import com.alan.clients.util.shader.base.ShaderRenderType;
import com.alan.clients.util.shader.base.ShaderUniforms;
import com.alan.clients.util.shader.kernel.GaussianKernel;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.nio.FloatBuffer;
import java.util.List;

public class GaussianBlurShader extends RiseShader {

    private final RiseShaderProgram blurProgram = new RiseShaderProgram("blur.frag", "vertex.vsh");
    private Framebuffer inputFramebuffer = new Framebuffer(mc.displayWidth / 2, mc.displayHeight / 2, true);
    private Framebuffer outputFramebuffer = new Framebuffer(mc.displayWidth / 2, mc.displayHeight / 2, true);
    private GaussianKernel gaussianKernel = new GaussianKernel(0);

    private int radius;
    private float compression;

    public GaussianBlurShader(){
        this(12);
    }

    public GaussianBlurShader(int radius){
        this.radius = radius;
        this.compression = 1.0f;
    }

    @Override
    public void run(final ShaderRenderType type, final float partialTicks, List<Runnable> runnable) {

        switch (type) {
            case CAMERA: {
                this.update();
                this.setActive(!runnable.isEmpty());

                if (this.isActive()) {
                    this.inputFramebuffer.bindFramebuffer(true);
                    runnable.forEach(Runnable::run);
                    mc.getFramebuffer().bindFramebuffer(true);
                }
                break;
            }
            case OVERLAY: {
                this.setActive(this.isActive() || !runnable.isEmpty());

                if (this.isActive()) {
                    this.inputFramebuffer.bindFramebuffer(true);
                    runnable.forEach(Runnable::run);

                    final int programId = this.blurProgram.getProgramId();

                    this.outputFramebuffer.bindFramebuffer(true);
                    this.blurProgram.start();

                    if (this.gaussianKernel.getSize() != radius) {
                        this.gaussianKernel = new GaussianKernel(radius);
                        this.gaussianKernel.compute();

                        final FloatBuffer buffer = BufferUtils.createFloatBuffer(radius);
                        buffer.put(this.gaussianKernel.getKernel());
                        buffer.flip();

                        ShaderUniforms.uniform1f(programId, "u_radius", radius);
                        ShaderUniforms.uniformFB(programId, "u_kernel", buffer);
                        ShaderUniforms.uniform1i(programId, "u_diffuse_sampler", 0);
                        ShaderUniforms.uniform1i(programId, "u_other_sampler", 20);
                    }

                    ShaderUniforms.uniform2f(programId, "u_texel_size", 1.0F / (mc.displayWidth / 2), 1.0F / (mc.displayHeight / 2));
                    ShaderUniforms.uniform2f(programId, "u_direction", compression, 0.0F);

                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
                    mc.getFramebuffer().bindFramebufferTexture();
                    RiseShaderProgram.drawQuad();

                    mc.getFramebuffer().bindFramebuffer(true);
                    ShaderUniforms.uniform2f(programId, "u_direction", 0.0F, compression);
                    outputFramebuffer.bindFramebufferTexture();
                    GL13.glActiveTexture(GL13.GL_TEXTURE20);
                    inputFramebuffer.bindFramebufferTexture();
                    GL13.glActiveTexture(GL13.GL_TEXTURE0);
                    RiseShaderProgram.drawQuad();
                    GlStateManager.disableBlend();

                    RiseShaderProgram.stop();
                }

                break;
            }
        }
    }

    @Override
    public void update() {
        this.setActive(false);

        if (mc.displayWidth / 2 != inputFramebuffer.framebufferWidth || mc.displayHeight / 2 != inputFramebuffer.framebufferHeight) {
            inputFramebuffer.deleteFramebuffer();
            inputFramebuffer = new Framebuffer(mc.displayWidth / 2, mc.displayHeight / 2, true);

            outputFramebuffer.deleteFramebuffer();
            outputFramebuffer = new Framebuffer(mc.displayWidth / 2, mc.displayHeight / 2, true);
        } else {
            inputFramebuffer.framebufferClear();
            outputFramebuffer.framebufferClear();
        }
    }
}
