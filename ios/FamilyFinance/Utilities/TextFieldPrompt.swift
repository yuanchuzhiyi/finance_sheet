import SwiftUI

#if canImport(UIKit)
import UIKit

struct TextFieldPrompt: UIViewControllerRepresentable {
    struct Prompt: Identifiable {
        let id = UUID()
        let title: String
        var message: String? = nil
        var placeholder: String? = nil
        var initialText: String = ""
        var keyboardType: UIKeyboardType = .default
        var confirmTitle: String = "确定"
        var cancelTitle: String = "取消"
        var onSubmit: (String) -> Void
    }

    @Binding var prompt: Prompt?

    func makeUIViewController(context: Context) -> UIViewController {
        UIViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        guard let prompt else {
            context.coordinator.presentedPromptId = nil
            return
        }

        guard context.coordinator.presentedPromptId != prompt.id else { return }
        context.coordinator.presentedPromptId = prompt.id

        let alert = UIAlertController(title: prompt.title, message: prompt.message, preferredStyle: .alert)
        alert.addTextField { textField in
            textField.text = prompt.initialText
            textField.placeholder = prompt.placeholder
            textField.keyboardType = prompt.keyboardType
        }

        alert.addAction(UIAlertAction(title: prompt.cancelTitle, style: .cancel) { _ in
            self.prompt = nil
        })

        alert.addAction(UIAlertAction(title: prompt.confirmTitle, style: .default) { _ in
            let text = (alert.textFields?.first?.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
            self.prompt = nil
            if !text.isEmpty {
                prompt.onSubmit(text)
            }
        })

        DispatchQueue.main.async {
            uiViewController.present(alert, animated: true)
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    final class Coordinator {
        var presentedPromptId: UUID?
    }
}

extension View {
    func textFieldPrompt(_ prompt: Binding<TextFieldPrompt.Prompt?>) -> some View {
        background(TextFieldPrompt(prompt: prompt))
    }
}
#endif

