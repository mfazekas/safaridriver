﻿using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace OpenQA.Selenium.Firefox.Internal
{
    /// <summary>
    /// Provides a mutex-like lock on a socket.
    /// </summary>
    internal class SocketLock : ILock
    {
        #region Private members
        private static int delayBetweenSocketChecks = 100;

        private int lockPort;
        private Socket lockSocket; 
        #endregion

        #region Constructor
        /// <summary>
        /// Initializes a new instance of the <see cref="SocketLock"/> class.
        /// </summary>
        /// <param name="lockPort">Port to use to acquire the lock.</param>
        /// <remarks>The <see cref="SocketLock"/> class will attempt to acquire the
        /// specified port number, and wait for it to become free.</remarks>
        public SocketLock(int lockPort)
        {
            this.lockPort = lockPort;
            lockSocket = new Socket(AddressFamily.InterNetwork, SocketType.Raw, ProtocolType.Icmp);
        } 
        #endregion

        #region ILock Members
        /// <summary>
        /// Locks the mutex port.
        /// </summary>
        /// <param name="timeoutInMilliseconds">The amount of time (in milliseconds) to wait for 
        /// the mutex port to become available.</param>
        public void LockObject(long timeoutInMilliseconds)
        {
            IPHostEntry hostEntry = Dns.GetHostEntry("localhost");

            //Use the first IPv4 address that we find
            IPAddress ipAddress = IPAddress.Parse("127.0.0.1");
            foreach (IPAddress ip in hostEntry.AddressList)
            {
                if (ip.AddressFamily == AddressFamily.InterNetwork)
                {
                    ipAddress = ip;
                    break;
                }
            }

            IPEndPoint address = new IPEndPoint(ipAddress, lockPort);

            // Calculate the 'exit time' for our wait loop.
            DateTime maxWait = DateTime.Now.AddMilliseconds(timeoutInMilliseconds);

            // Attempt to acquire the lock until something goes wrong or we run out of time.
            do
            {
                try
                {
                    if (IsLockFree(address))
                    {
                        return;
                    }

                    Thread.Sleep(delayBetweenSocketChecks);
                }
                catch (ThreadInterruptedException e)
                {
                    throw new WebDriverException("the thread was interrupted", e);
                }
                catch (IOException e)
                {
                    throw new WebDriverException("An unexpected error occured", e);
                }
            }
            while (DateTime.Now < maxWait);

            throw new WebDriverException(string.Format(CultureInfo.InvariantCulture, "Unable to bind to locking port {0} within {1} ms", lockPort, timeoutInMilliseconds));
        }

        /// <summary>
        /// Unlocks the mutex port.
        /// </summary>
        public void UnlockObject()
        {
            try
            {
                lockSocket.Close();
            }
            catch (IOException e)
            {
                throw new WebDriverException("An error occured unlocking the object", e);
            }
        }
        #endregion

        #region IDisposable Members
        /// <summary>
        /// Releases all resources associated with this <see cref="SocketLock"/>
        /// </summary>
        public void Dispose()
        {
            if (lockSocket != null && lockSocket.Connected)
            {
                lockSocket.Close();
            }

            GC.SuppressFinalize(this);
        }
        #endregion

        #region Support methods
        private bool IsLockFree(IPEndPoint address)
        {
            try
            {
                lockSocket.Bind(address);
                return true;
            }
            catch (SocketException)
            {
                return false;
            }
        } 
        #endregion
    }
}
