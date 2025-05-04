//
//  Colors.swift
//  YabaSnashiro
//
//  Created by Shinya Miyamoto on 2024/08/01.
//  Copyright © 2024 devMiyax. All rights reserved.
//

import UIKit

extension UIColor {
    // MARK: - App Colors
    
    // 基本色
    static var backgroundGradientStart: UIColor {
        return UIColor(named: "backgroundGradientStart") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x000000) : UIColor(hex: 0x000000)
        }
    }
    
    static var backgroundGradientEnd: UIColor {
        return UIColor(named: "backgroundGradientEnd") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0xDDDDDD) : UIColor(hex: 0xDDDDDD)
        }
    }
    
    static var fastlaneBackground: UIColor {
        return UIColor(named: "fastlaneBackground") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x1c1f24) : UIColor(hex: 0x1c1f24)
        }
    }
    
    static var searchOpaque: UIColor {
        return UIColor(named: "searchOpaque") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x4444DD) : UIColor(hex: 0x4444DD)
        }
    }
    
    static var selectedBackground: UIColor {
        return UIColor(named: "selectedBackground") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x6f7c91) : UIColor(hex: 0x6f7c91)
        }
    }
    
    static var detailBackground: UIColor {
        return UIColor(named: "detailBackground") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x0096a6) : UIColor(hex: 0x0096a6)
        }
    }
    
    static var softOpaque: UIColor {
        return UIColor(named: "softOpaque") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x000000, alpha: 0.3) : UIColor(hex: 0x000000, alpha: 0.3)
        }
    }
    
    static var imgSoftOpaque: UIColor {
        return UIColor(named: "imgSoftOpaque") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0xFF0000, alpha: 0.3) : UIColor(hex: 0xFF0000, alpha: 0.3)
        }
    }
    
    static var imgFullOpaque: UIColor {
        return UIColor(named: "imgFullOpaque") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x000000, alpha: 0.0) : UIColor(hex: 0x000000, alpha: 0.0)
        }
    }
    
    static var blackOpaque: UIColor {
        return UIColor(named: "blackOpaque") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x000000, alpha: 0.67) : UIColor(hex: 0x000000, alpha: 0.67)
        }
    }
    
    // 基本色（ダークモード対応）
    static var appBlack: UIColor {
        return UIColor(named: "appBlack") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x000000) : UIColor(hex: 0x000000)
        }
    }
    
    static var appWhite: UIColor {
        return UIColor(named: "appWhite") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0xFFFFFF) : UIColor(hex: 0xFFFFFF)
        }
    }
    
    static var appDisable: UIColor {
        return UIColor(named: "appDisable") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x808080) : UIColor(hex: 0x808080)
        }
    }
    
    static var orangeTransparent: UIColor {
        return UIColor(named: "orangeTransparent") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0xFADCA7, alpha: 0.67) : UIColor(hex: 0xFADCA7, alpha: 0.67)
        }
    }
    
    static var appOrange: UIColor {
        return UIColor(named: "appOrange") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0xFADCA7) : UIColor(hex: 0xFADCA7)
        }
    }
    
    static var appYellow: UIColor {
        return UIColor(named: "appYellow") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0xEEFF41) : UIColor(hex: 0xEEFF41)
        }
    }
    
    static var defaultBackground: UIColor {
        return UIColor(named: "defaultBackground") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x121212) : UIColor(hex: 0xFFFFFF)
        }
    }
    
    static var colorPrimary: UIColor {
        return UIColor(named: "colorPrimary") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x6F7C91) : UIColor(hex: 0x6F7C91)
        }
    }
    
    static var colorPrimaryDark: UIColor {
        return UIColor(named: "colorPrimaryDark") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x373E48) : UIColor(hex: 0x373E48)
        }
    }
    
    static var colorAccent: UIColor {
        return UIColor(named: "colorAccent") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0xB7BEC8) : UIColor(hex: 0xB7BEC8)
        }
    }
    
    static var halfTransparent: UIColor {
        return UIColor(named: "halfTransparent") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x000000, alpha: 0.8) : UIColor(hex: 0x000000, alpha: 0.8)
        }
    }
    
    static var cardviewInitialBackground: UIColor {
        return UIColor(named: "cardviewInitialBackground") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x6f7c91) : UIColor(hex: 0x6f7c91)
        }
    }
    
    static var titleBack: UIColor {
        return UIColor(named: "titleBack") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0xdadee2) : UIColor(hex: 0xdadee2)
        }
    }
    
    static var appError: UIColor {
        return UIColor(named: "appError") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0xCF6679) : UIColor(hex: 0xCF6679)
        }
    }
    
    static var secondary: UIColor {
        return UIColor(named: "secondary") ?? UIColor { traitCollection in
            return traitCollection.userInterfaceStyle == .dark ? UIColor(hex: 0x4444DD) : UIColor(hex: 0x4444DD)
        }
    }
    
    // MARK: - Hex Initializer
    
    convenience init(hex: Int, alpha: CGFloat = 1.0) {
        let red = CGFloat((hex & 0xFF0000) >> 16) / 255.0
        let green = CGFloat((hex & 0x00FF00) >> 8) / 255.0
        let blue = CGFloat(hex & 0x0000FF) / 255.0
        
        self.init(red: red, green: green, blue: blue, alpha: alpha)
    }
}
