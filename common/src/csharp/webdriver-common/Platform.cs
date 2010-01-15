﻿/* Copyright notice and license
Copyright 2007-2010 WebDriver committers
Copyright 2007-2010 Google Inc.
Portions copyright 2007 ThoughtWorks, Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

using System;
using System.Collections.Generic;
using System.Text;

namespace OpenQA.Selenium
{
    /// <summary>
    /// Represents the known and supported Platforms that WebDriver runs on.
    /// </summary>
    /// <remarks>The <see cref="Platform"/> class maps closely to the Operating System, 
    /// but differs slightly, because this class is used to extract information such as 
    /// program locations and line endings. </remarks>
    public enum PlatformType
    {
        /// <summary>
        /// Any platform. This value is never returned by a driver, but can be used to find
        /// drivers with certain capabilities.
        /// </summary>
        Any,

        /// <summary>
        /// Any version of Microsoft Windows. This value is never returned by a driver, 
        /// but can be used to find drivers with certain capabilities.
        /// </summary>
        Windows,

        /// <summary>
        /// Versions of Microsoft Windows that are compatible with Windows XP.
        /// </summary>
        XP,

        /// <summary>
        /// Versions of Microsoft Windows that are compatible with Windows Vista.
        /// </summary>
        Vista,

        /// <summary>
        /// Any version of the Macintosh OS X
        /// </summary>
        MacOSX,

        /// <summary>
        /// Any version of the Unix operating system.
        /// </summary>
        Unix,

        /// <summary>
        /// Any version of the Linux operating system.
        /// </summary>
        Linux
    }

    /// <summary>
    /// Represents the platform on which tests are to be run.
    /// </summary>
    public class Platform
    {
        private static Platform current;
        private PlatformType platformTypeValue;
        private int major;
        private int minor;

        /// <summary>
        /// Initializes a new instance of the <see cref="Platform"/> class for a specific platform type.
        /// </summary>
        /// <param name="typeValue">The platform type.</param>
        public Platform(PlatformType typeValue)
        {
            platformTypeValue = typeValue;
        }

        private Platform()
        {
            major = Environment.OSVersion.Version.Major;
            minor = Environment.OSVersion.Version.Minor;

            switch (Environment.OSVersion.Platform)
            {
                case PlatformID.Win32NT:
                    if (major == 5)
                    {
                        platformTypeValue = PlatformType.XP;
                    }
                    else if (major == 6)
                    {
                        platformTypeValue = PlatformType.Vista;
                    }

                    break;

                case PlatformID.MacOSX:
                    platformTypeValue = PlatformType.MacOSX;
                    break;

                case PlatformID.Unix:
                    platformTypeValue = PlatformType.Unix;
                    break;
            }
        }

        /// <summary>
        /// Gets the current platform.
        /// </summary>
        public static Platform CurrentPlatform
        {
            get 
            {
                if (current == null)
                {
                    current = new Platform();
                }

                return current;
            }
        }

        /// <summary>
        /// Gets the major version of the platform operating system.
        /// </summary>
        public int MajorVersion
        {
            get { return major; }
        }

        /// <summary>
        /// Gets the major version of the platform operating system.
        /// </summary>
        public int MinorVersion
        {
            get { return minor; }
        }

        /// <summary>
        /// Gets the type of the platform.
        /// </summary>
        public PlatformType Type
        {
            get { return platformTypeValue; }
        }

        /// <summary>
        /// Compares the platform to the specified type.
        /// </summary>
        /// <param name="compareTo">A <see cref="PlatformType"/> value to compare to.</param>
        /// <returns><see langword="true"/> if the platforms match; otherwise <see langword="false"/>.</returns>
        public bool IsPlatformType(PlatformType compareTo)
        {
            bool platformIsType = false;
            switch (compareTo)
            {
                case PlatformType.Any:
                    platformIsType = true;
                    break;

                case PlatformType.Windows:
                    platformIsType = platformTypeValue == PlatformType.Windows || platformTypeValue == PlatformType.XP || platformTypeValue == PlatformType.Vista;
                    break;

                case PlatformType.Vista:
                    platformIsType = platformTypeValue == PlatformType.Windows || platformTypeValue == PlatformType.Vista;
                    break;

                case PlatformType.XP:
                    platformIsType = platformTypeValue == PlatformType.Windows || platformTypeValue == PlatformType.XP;
                    break;

                case PlatformType.Linux:
                    platformIsType = platformTypeValue == PlatformType.Linux || platformTypeValue == PlatformType.Unix;
                    break;

                default:
                    platformIsType = platformTypeValue == compareTo;
                    break;
            }

            return platformIsType;
        }
    }
}
