/**
 * Copyright 2010 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */

package com.jogamp.opengl.test.junit.jogl.acore.anim;

import java.lang.reflect.InvocationTargetException;

import com.jogamp.opengl.GLCapabilities;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.test.junit.util.NewtTestUtil;
import com.jogamp.opengl.test.junit.util.GLTestUtil;
import com.jogamp.opengl.test.junit.util.UITestCase;
import com.jogamp.opengl.test.junit.jogl.demos.es2.GearsES2;

import org.junit.Assert;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestAnimatorGLWindow01NEWT extends UITestCase {
    static final int width = 400;
    static final int height = 400;

    protected GLWindow createGLWindow(final GLCapabilities caps, final int x, final int y, final GearsES2 gears) throws InterruptedException {
        final GLWindow glWindow = GLWindow.create(caps);
        Assert.assertNotNull(glWindow);
        glWindow.addGLEventListener(gears);
        glWindow.setPosition(x, y);
        glWindow.setSize(width, height);
        glWindow.setTitle("GLWindow: "+x+"/"+y);
        return glWindow;
    }

    static void pauseAnimator(final Animator animator, final boolean pause) {
        if(pause) {
            animator.pause();
            Assert.assertEquals(true, animator.isStarted());
            Assert.assertEquals(true, animator.isPaused());
            Assert.assertEquals(false, animator.isAnimating());
        } else {
            animator.resume();
            Assert.assertEquals(true, animator.isStarted());
            Assert.assertEquals(false, animator.isPaused());
            Assert.assertEquals(true, animator.isAnimating());
        }
    }
    static void stopAnimator(final Animator animator) {
        animator.stop();
        Assert.assertEquals(false, animator.isStarted());
        Assert.assertEquals(false, animator.isPaused());
        Assert.assertEquals(false, animator.isAnimating());
    }

    @Test
    public void test01SyncedOneAnimator() throws InterruptedException, InvocationTargetException {
        final GLCapabilities caps = new GLCapabilities(null);
        final Animator animator = new Animator();
        animator.start();
        Assert.assertEquals(true, animator.isStarted());
        Assert.assertEquals(true, animator.isPaused());
        Assert.assertEquals(false, animator.isAnimating());

        final GearsES2 g1 = new GearsES2(0);
        final GLWindow c1 = createGLWindow(caps, 0, 0, g1);
        animator.add(c1);
        Assert.assertEquals(true, animator.isStarted());
        Assert.assertEquals(false, animator.isPaused());
        Assert.assertEquals(true, animator.isAnimating());

        final GearsES2 g2 = new GearsES2(0);
        final GLWindow c2 = createGLWindow(caps, c1.getX()+width,
                                           c1.getY()+0, g2);
        animator.add(c2);

        final GearsES2 g3 = new GearsES2(0);
        final GLWindow c3 = createGLWindow(caps, c1.getX()+0,
                                           c1.getY()+height, g3);
        animator.add(c3);

        c1.setVisible(true);
        c2.setVisible(true);
        c3.setVisible(true);

        Assert.assertTrue(NewtTestUtil.waitForRealized(c1, true, null));
        Assert.assertTrue(NewtTestUtil.waitForVisible(c1, true, null));
        Assert.assertTrue(GLTestUtil.waitForContextCreated(c1, true, null));
        Assert.assertTrue("Gears1 not initialized", g1.waitForInit(true));

        Assert.assertTrue(NewtTestUtil.waitForRealized(c2, true, null));
        Assert.assertTrue(NewtTestUtil.waitForVisible(c2, true, null));
        Assert.assertTrue(GLTestUtil.waitForContextCreated(c2, true, null));
        Assert.assertTrue("Gears2 not initialized", g2.waitForInit(true));

        Assert.assertTrue(NewtTestUtil.waitForRealized(c3, true, null));
        Assert.assertTrue(NewtTestUtil.waitForVisible(c3, true, null));
        Assert.assertTrue(GLTestUtil.waitForContextCreated(c3, true, null));
        Assert.assertTrue("Gears3 not initialized", g3.waitForInit(true));

        try {
            Thread.sleep(duration/3);
        } catch(final Exception e) {
            e.printStackTrace();
        }

        pauseAnimator(animator, true);

        try {
            Thread.sleep(duration/3);
        } catch(final Exception e) {
            e.printStackTrace();
        }

        pauseAnimator(animator, false);

        try {
            Thread.sleep(duration/3);
        } catch(final Exception e) {
            e.printStackTrace();
        }

        // Stopped animator allows native windowing system 'repaint' event
        // to trigger GLAD 'display'
        stopAnimator(animator);

        c1.destroy();
        c2.destroy();
        c3.destroy();

        Assert.assertTrue(NewtTestUtil.waitForRealized(c1, false, null));
        Assert.assertTrue(NewtTestUtil.waitForRealized(c2, false, null));
        Assert.assertTrue(NewtTestUtil.waitForRealized(c3, false, null));
    }

    @Test
    public void test02AsyncEachAnimator() throws InterruptedException, InvocationTargetException {
        final GLCapabilities caps = new GLCapabilities(null);
        final Animator a1 = new Animator();
        final GearsES2 g1 = new GearsES2(0);
        final GLWindow c1 = createGLWindow(caps, 0, 0, g1);
        a1.add(c1);
        a1.start();
        Assert.assertEquals(true, a1.isStarted());
        Assert.assertEquals(false, a1.isPaused());
        Assert.assertEquals(true, a1.isAnimating());
        c1.setVisible(true);

        final Animator a2 = new Animator();
        final GearsES2 g2 = new GearsES2(0);
        final GLWindow c2 = createGLWindow(caps, c1.getX()+width, c1.getY()+0, g2);
        a2.add(c2);
        a2.start();
        Assert.assertEquals(true, a2.isStarted());
        Assert.assertEquals(false, a2.isPaused());
        Assert.assertEquals(true, a2.isAnimating());
        c2.setVisible(true);

        final Animator a3 = new Animator();
        final GearsES2 g3 = new GearsES2(0);
        final GLWindow c3 = createGLWindow(caps, c1.getX()+0, c1.getY()+height, g3);
        a3.add(c3);
        a3.start();
        Assert.assertEquals(true, a3.isStarted());
        Assert.assertEquals(false, a3.isPaused());
        Assert.assertEquals(true, a3.isAnimating());
        c3.setVisible(true);

        Assert.assertTrue(NewtTestUtil.waitForRealized(c1, true, null));
        Assert.assertTrue(NewtTestUtil.waitForVisible(c1, true, null));
        Assert.assertTrue(GLTestUtil.waitForContextCreated(c1, true, null));
        Assert.assertTrue("Gears1 not initialized", g1.waitForInit(true));

        Assert.assertTrue(NewtTestUtil.waitForRealized(c2, true, null));
        Assert.assertTrue(NewtTestUtil.waitForVisible(c2, true, null));
        Assert.assertTrue(GLTestUtil.waitForContextCreated(c2, true, null));
        Assert.assertTrue("Gears2 not initialized", g2.waitForInit(true));

        Assert.assertTrue(NewtTestUtil.waitForRealized(c3, true, null));
        Assert.assertTrue(NewtTestUtil.waitForVisible(c3, true, null));
        Assert.assertTrue(GLTestUtil.waitForContextCreated(c3, true, null));
        Assert.assertTrue("Gears3 not initialized", g3.waitForInit(true));

        try {
            Thread.sleep(duration/3);
        } catch(final Exception e) {
            e.printStackTrace();
        }

        pauseAnimator(a1, true);
        pauseAnimator(a2, true);
        pauseAnimator(a3, true);

        try {
            Thread.sleep(duration/3);
        } catch(final Exception e) {
            e.printStackTrace();
        }

        pauseAnimator(a1, false);
        pauseAnimator(a2, false);
        pauseAnimator(a3, false);

        try {
            Thread.sleep(duration/3);
        } catch(final Exception e) {
            e.printStackTrace();
        }

        // Stopped animator allows native windowing system 'repaint' event
        // to trigger GLAD 'display'
        stopAnimator(a1);
        stopAnimator(a2);
        stopAnimator(a3);

        c1.destroy();
        c2.destroy();
        c3.destroy();

        Assert.assertTrue(NewtTestUtil.waitForRealized(c1, false, null));
        Assert.assertTrue(NewtTestUtil.waitForRealized(c2, false, null));
        Assert.assertTrue(NewtTestUtil.waitForRealized(c3, false, null));
    }

    static long duration = 3*500; // ms

    public static void main(final String args[]) {
        for(int i=0; i<args.length; i++) {
            if(args[i].equals("-time")) {
                i++;
                try {
                    duration = Integer.parseInt(args[i]);
                } catch (final Exception ex) { ex.printStackTrace(); }
            }
        }
        /**
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        System.err.println("Press enter to continue");
        System.err.println(stdin.readLine()); */
        org.junit.runner.JUnitCore.main(TestAnimatorGLWindow01NEWT.class.getName());
    }
}
