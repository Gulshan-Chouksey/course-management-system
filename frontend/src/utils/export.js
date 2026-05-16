/**
 * Export utilities for CSV and PDF download
 */

// Convert array of objects to CSV string
function toCSV(headers, rows) {
  const escape = (val) => {
    const str = String(val ?? '');
    return str.includes(',') || str.includes('"') || str.includes('\n')
      ? `"${str.replace(/"/g, '""')}"` : str;
  };
  const lines = [headers.map(h => escape(h.label)).join(',')];
  rows.forEach(row => {
    lines.push(headers.map(h => escape(row[h.key])).join(','));
  });
  return lines.join('\n');
}

// Trigger file download in browser
function downloadFile(content, filename, mimeType) {
  const blob = new Blob([content], { type: mimeType });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

// Export data as CSV
export function exportCSV(headers, rows, filename = 'report') {
  const csv = toCSV(headers, rows);
  downloadFile(csv, `${filename}.csv`, 'text/csv;charset=utf-8;');
}

// Export transcript or report as printable HTML (opens print dialog for PDF)
export function exportPDF(title, htmlContent) {
  const win = window.open('', '_blank');
  if (!win) return;
  win.document.write(`
    <!DOCTYPE html>
    <html><head><title>${title}</title>
    <style>
      body { font-family: 'Segoe UI', Arial, sans-serif; margin: 40px; color: #1a1a2e; }
      h1 { font-size: 22px; border-bottom: 2px solid #6366f1; padding-bottom: 8px; }
      h2 { font-size: 16px; color: #555; margin-top: 24px; }
      table { width: 100%; border-collapse: collapse; margin-top: 12px; }
      th, td { padding: 10px 12px; border: 1px solid #ddd; text-align: left; font-size: 13px; }
      th { background: #f5f5ff; font-weight: 600; }
      .meta { display: flex; gap: 32px; margin: 16px 0; font-size: 14px; }
      .meta span { color: #666; }
      .meta strong { color: #1a1a2e; }
      .badge { padding: 3px 10px; border-radius: 4px; font-weight: 700; font-size: 12px; background: #e8e8ff; color: #6366f1; }
      .footer { margin-top: 32px; font-size: 11px; color: #999; text-align: center; border-top: 1px solid #eee; padding-top: 12px; }
      @media print { body { margin: 20px; } }
    </style></head><body>
    ${htmlContent}
    <div class="footer">Generated on ${new Date().toLocaleString()} — Course Management System</div>
    </body></html>
  `);
  win.document.close();
  setTimeout(() => win.print(), 500);
}

// Build transcript PDF content
export function buildTranscriptHTML(transcript) {
  if (!transcript) return '';
  let html = `<h1>Academic Transcript</h1>`;
  html += `<div class="meta">
    <div><span>Student: </span><strong>${transcript.studentName}</strong></div>
    <div><span>ID: </span><strong>${transcript.studentId}</strong></div>
    <div><span>Department: </span><strong>${transcript.department}</strong></div>
  </div>`;
  html += `<div class="meta">
    <div><span>GPA: </span><strong>${transcript.gpa?.toFixed(2) || 'N/A'} / 4.0</strong></div>
    <div><span>Total Courses: </span><strong>${transcript.totalCourses || 0}</strong></div>
    <div><span>Total Credits: </span><strong>${transcript.totalCredits || 0}</strong></div>
  </div>`;
  if (transcript.courses?.length > 0) {
    html += `<h2>Course Grades</h2><table>
      <tr><th>Code</th><th>Course</th><th>Semester</th><th>Credits</th><th>Internal</th><th>External</th><th>Total</th><th>Grade</th></tr>`;
    transcript.courses.forEach(c => {
      html += `<tr><td>${c.courseCode}</td><td>${c.courseName}</td><td>${c.semester || '—'}</td>
        <td>${c.credits}</td><td>${c.internalMarks}</td><td>${c.externalMarks}</td>
        <td><strong>${c.totalMarks}</strong></td><td><span class="badge">${c.grade}</span></td></tr>`;
    });
    html += `</table>`;
  }
  return html;
}
